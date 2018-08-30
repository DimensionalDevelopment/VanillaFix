package org.dimdev.vanillafix.blockstates;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dimdev.utils.LeftIdentityPair;

import javax.annotation.Nullable;
import java.util.*;

/**
 * An implementation of IBlockState which stores the properties in a bitfield rather
 * than a table, for better memory usage and performance. States are also not all kept
 * in memory forever, but it is guaranteed that there won't ever be two different objects
 * for the same state using the 'loadedBlockStates' WeakHashMap (for efficiency, and to
 * make comparison using == still work).
 */
@SuppressWarnings("deprecation")
public class NumericalBlockState extends BlockStateBase {
    private static final Map<LeftIdentityPair<BlockStateContainer, Integer>, NumericalBlockState> blockStates = new HashMap<>(); // TODO: WeakHashMap?
    private static final Map<LeftIdentityPair<IProperty<?>, Comparable<?>>, Integer> valueToNumber = new HashMap<>();
    private static final Map<LeftIdentityPair<IProperty<?>, Integer>, Comparable<?>> numberToValue = new HashMap<>();
    private static final Map<IProperty<?>, Integer> propertyWidths = new IdentityHashMap<>();

    protected final BlockStateContainer container;
    protected final Block block;
    protected final int data; // TODO: short

    protected NumericalBlockState(BlockStateContainer container, int data) {
        this.container = container;
        block = container.getBlock();
        this.data = data;
    }

    public static NumericalBlockState get(BlockStateContainer container, int data) {
        // Getting it from a cache is necessary to make sure == between two NumericalBlockStates
        // with the same container and data will work. The cache is shared for all containers
        // to avoid the overhead of many small HashMaps.
        LeftIdentityPair<BlockStateContainer, Integer> key = new LeftIdentityPair<>(container, data);
        NumericalBlockState blockState = blockStates.get(key);

        if (blockState == null) {
            blockState = new NumericalBlockState(container, data);
            blockStates.put(key, blockState);
        }

        return blockState;
    }

    public static NumericalBlockState fromPropertyValueMap(BlockStateContainer container, Map<IProperty<?>, Comparable<?>> map) {
        Map<IProperty<?>, Integer> offsets = ((IPatchedBlockStateContainer) container).getPropertyOffsets();

        int data = 0;
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : map.entrySet()) {
            IProperty<?> property = entry.getKey();
            data |= valueToNumber.get(new LeftIdentityPair<>(property, entry.getValue())) << offsets.get(property);
        }

        return get(container, data);
    }

    public static <T extends Comparable<T>> void makePropertyInfo(IProperty<T> property) {
        if (propertyWidths.containsKey(property)) return;

        Collection<T> allowedValues = property.getAllowedValues();

        // Calculate width of the property's number in the bit field
        propertyWidths.put(property, MathHelper.log2(allowedValues.size()) + 1);

        // Fill the 'number -> value' and 'value -> number' maps
        int i = 0;
        for (T value : allowedValues) {
            numberToValue.put(new LeftIdentityPair<>(property, i), value);
            valueToNumber.put(new LeftIdentityPair<>(property, value), i);
            i++;
        }
    }

    @Override
    public Collection<IProperty<?>> getPropertyKeys() {
        return container.getProperties();
    }

    @Override
    public <T extends Comparable<T>> T getValue(IProperty<T> property) {
        int offset = ((IPatchedBlockStateContainer) container).getPropertyOffsets().getOrDefault(property, -1);

        if (offset == -1) {
            throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + container);
        }

        int width = propertyWidths.get(property);
        int number = data >>> offset & 0xFFFFFFFF >>> 32 - width;
        Comparable<?> value = numberToValue.get(new LeftIdentityPair<>(property, number));

        return property.getValueClass().cast(value);
    }

    @Override
    public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
        int offset = ((IPatchedBlockStateContainer) container).getPropertyOffsets().getOrDefault(property, -1);

        if (offset == -1) {
            throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + container);
        }

        int number = valueToNumber.get(new LeftIdentityPair<>(property, value));
        int width = propertyWidths.get(property);
        int mask = (0xFFFFFFFF >>> offset & 0xFFFFFFFF >>> 32 - width) << offset;
        int newData = data & ~mask | number << offset;

        if (data == newData) {
            return this;
        } else {
            return get(container, newData);
        }
    }

    @Override
    public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
        ImmutableMap.Builder<IProperty<?>, Comparable<?>> properties = ImmutableMap.builder();

        for (IProperty<?> property : container.getProperties()) {
            properties.put(property, getValue(property));
        }

        return properties.build();
    }

    @Override
    public int hashCode() {
        return data ^ container.hashCode();
    }

    @Override
    public Block getBlock() {
        return block;
    }

    // <editor-fold defaultstate="collapsed" desc="Methods proxied to block class">
    @Override
    public Material getMaterial() {
        return block.getMaterial(this);
    }

    @Override
    public boolean isFullBlock() {
        return block.isFullBlock(this);
    }

    @Override
    public boolean canEntitySpawn(Entity entity) {
        return block.canEntitySpawn(this, entity);
    }

    @Override
    public int getLightOpacity() {
        return block.getLightOpacity(this);
    }

    @Override
    public int getLightValue() {
        return block.getLightValue(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isTranslucent() {
        return block.isTranslucent(this);
    }

    @Override
    public boolean useNeighborBrightness() {
        return block.getUseNeighborBrightness(this);
    }

    @Override
    public MapColor getMapColor(IBlockAccess p_185909_1_, BlockPos p_185909_2_) {
        return block.getMapColor(this, p_185909_1_, p_185909_2_);
    }

    @Override
    public IBlockState withRotation(Rotation rot) {
        return block.withRotation(this, rot);
    }

    @Override
    public IBlockState withMirror(Mirror mirror) {
        return block.withMirror(this, mirror);
    }

    @Override
    public boolean isFullCube() {
        return block.isFullCube(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasCustomBreakingProgress() {
        return block.hasCustomBreakingProgress(this);
    }

    @Override
    public EnumBlockRenderType getRenderType() {
        return block.getRenderType(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockAccess source, BlockPos pos) {
        return block.getPackedLightmapCoords(this, source, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue() {
        return block.getAmbientOcclusionLightValue(this);
    }

    @Override
    public boolean isBlockNormalCube() {
        return block.isBlockNormalCube(this);
    }

    @Override
    public boolean isNormalCube() {
        return block.isNormalCube(this);
    }

    @Override
    public boolean canProvidePower() {
        return block.canProvidePower(this);
    }

    @Override
    public int getWeakPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return block.getWeakPower(this, blockAccess, pos, side);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return block.hasComparatorInputOverride(this);
    }

    @Override
    public int getComparatorInputOverride(World world, BlockPos pos) {
        return block.getComparatorInputOverride(this, world, pos);
    }

    @Override
    public float getBlockHardness(World world, BlockPos pos) {
        return block.getBlockHardness(this, world, pos);
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, BlockPos pos) {
        return block.getPlayerRelativeBlockHardness(this, player, world, pos);
    }

    @Override
    public int getStrongPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return block.getStrongPower(this, blockAccess, pos, side);
    }

    @Override
    public EnumPushReaction getPushReaction() {
        return block.getPushReaction(this);
    }

    @Override
    public IBlockState getActualState(IBlockAccess blockAccess, BlockPos pos) {
        return block.getActualState(this, blockAccess, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
        return block.getSelectedBoundingBox(this, world, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing) {
        return block.shouldSideBeRendered(this, blockAccess, pos, facing);
    }

    @Override
    public boolean isOpaqueCube() {
        return block.isOpaqueCube(this);
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockAccess world, BlockPos pos) {
        return block.getCollisionBoundingBox(this, world, pos);
    }

    @Override
    public void addCollisionBoxToList(World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean p_185908_6_) {
        block.addCollisionBoxToList(this, world, pos, entityBox, collidingBoxes, entity, p_185908_6_);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockAccess blockAccess, BlockPos pos) {
        return block.getBoundingBox(this, blockAccess, pos);
    }

    @Override
    public RayTraceResult collisionRayTrace(World world, BlockPos pos, Vec3d start, Vec3d end) {
        return block.collisionRayTrace(this, world, pos, start, end);
    }

    @Override
    public boolean isTopSolid() {
        return block.isTopSolid(this);
    }

    @Override
    public Vec3d getOffset(IBlockAccess access, BlockPos pos) {
        return block.getOffset(this, access, pos);
    }

    @Override
    public boolean onBlockEventReceived(World world, BlockPos pos, int id, int param) {
        return block.eventReceived(this, world, pos, id, param);
    }

    @Override
    public void neighborChanged(World world, BlockPos pos, Block block, BlockPos fromPos) {
        this.block.neighborChanged(this, world, pos, block, fromPos);
    }

    @Override
    public boolean causesSuffocation() {
        return block.causesSuffocation(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, BlockPos pos, EnumFacing facing) {
        return block.getBlockFaceShape(world, this, pos, facing);
    }

    @Override
    public int getLightOpacity(IBlockAccess world, BlockPos pos) {
        return block.getLightOpacity(this, world, pos);
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        return block.getLightValue(this, world, pos);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return block.isSideSolid(this, world, pos, side);
    }

    @Override
    public boolean doesSideBlockChestOpening(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return block.doesSideBlockChestOpening(this, world, pos, side);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return block.doesSideBlockRendering(this, world, pos, side);
    }
    // </editor-fold>
}
