package org.dimdev.vanillafix.blockstates;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("deprecation")
public class NumericalExtendedBlockState extends BlockStateBase implements IExtendedBlockState {
    private static final Map<Pair<IBlockState, ImmutableMap<IUnlistedProperty<?>, Optional<?>>>, NumericalExtendedBlockState> cleanStates = new HashMap<>();

    private final NumericalExtendedBlockState cleanState;
    private final IBlockState normalState;
    private final ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties;

    public NumericalExtendedBlockState(IBlockState normalState, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        this(normalState, unlistedProperties, null);
    }

    private NumericalExtendedBlockState(IBlockState normalState, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties, NumericalExtendedBlockState cleanState) {
        this.normalState = normalState;
        this.unlistedProperties = unlistedProperties;
        this.cleanState = cleanState == null ? this : cleanState;
    }

    public static NumericalExtendedBlockState getClean(IBlockState normalState, ImmutableMap<IUnlistedProperty<?>, Optional<?>> unlistedProperties) {
        Pair<IBlockState, ImmutableMap<IUnlistedProperty<?>, Optional<?>>> key = new ImmutablePair<>(normalState, unlistedProperties);
        NumericalExtendedBlockState state = cleanStates.get(key);

        if (state == null) {
            state = new NumericalExtendedBlockState(normalState, unlistedProperties);
            cleanStates.put(key, state);
        }

        return state;
    }

    @Override
    public ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties() {
        return unlistedProperties;
    }

    @Override
    public Collection<IUnlistedProperty<?>> getUnlistedNames() {
        return unlistedProperties.keySet();
    }

    @Override
    public <V> V getValue(IUnlistedProperty<V> property) {
        Optional<?> value = unlistedProperties.get(property);
        if (value == null) {
            throw new IllegalArgumentException("Cannot get unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
        }

        return property.getType().cast(value.orElse(null));
    }

    @Override
    public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
        IBlockState clean = normalState.withProperty(property, value);
        if (clean == normalState) {
            return this;
        }

        if (this == cleanState) { // no dynamic properties present, looking up in the normal table
            return getClean(clean, unlistedProperties);
        }

        return new NumericalExtendedBlockState(clean, unlistedProperties, cleanState);
    }

    @Override
    public <V> IExtendedBlockState withProperty(IUnlistedProperty<V> property, @Nullable V value) {
        Optional<?> oldValue = unlistedProperties.get(property);
        if (oldValue == null) {
            throw new IllegalArgumentException("Cannot set unlisted property " + property + " as it does not exist in " + getBlock().getBlockState());
        }

        if (Objects.equals(oldValue.orElse(null), value)) {
            return this;
        }

        if (!property.isValid(value)) {
            throw new IllegalArgumentException("Cannot set unlisted property " + property + " to " + value + " on block " + Block.REGISTRY.getNameForObject(getBlock()) + ", it is not an allowed value");
        }

        boolean clean = true;
        ImmutableMap.Builder<IUnlistedProperty<?>, Optional<?>> builder = ImmutableMap.builder();
        for (Map.Entry<IUnlistedProperty<?>, Optional<?>> entry : unlistedProperties.entrySet()) {
            IUnlistedProperty<?> key = entry.getKey();
            Optional<?> newValue = key.equals(property) ? Optional.ofNullable(value) : entry.getValue();
            if (newValue.isPresent()) clean = false;
            builder.put(key, newValue);
        }

        if (clean) { // no dynamic properties, lookup normal state
            return cleanState;
        }

        return new NumericalExtendedBlockState(normalState, builder.build(), cleanState);
    }

    @Override
    public IBlockState getClean() {
        return cleanState;
    }

    // <editor-fold defaultstate="collapsed" desc="Methods proxied to normalState">
    @Override
    public Block getBlock() {
        return normalState.getBlock();
    }

    @Override
    public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
        return normalState.getProperties();
    }

    @Override
    public Collection<IProperty<?>> getPropertyKeys() {
        return normalState.getPropertyKeys();
    }

    @Override
    public <T extends Comparable<T>> T getValue(IProperty<T> property) {
        return normalState.getValue(property);
    }

    @Override
    public boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param) {
        return normalState.getBlock().eventReceived(this, worldIn, pos, id, param);
    }

    @Override
    public void neighborChanged(World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        normalState.getBlock().neighborChanged(this, worldIn, pos, blockIn, fromPos);
    }

    @Override
    public Material getMaterial() {
        return normalState.getBlock().getMaterial(this);
    }

    @Override
    public boolean isFullBlock() {
        return normalState.getBlock().isFullBlock(this);
    }

    @Override
    public boolean canEntitySpawn(Entity entityIn) {
        return normalState.getBlock().canEntitySpawn(this, entityIn);
    }

    @Override
    @Deprecated
    public int getLightOpacity() {
        return normalState.getBlock().getLightOpacity(this);
    }

    @Override
    public int getLightOpacity(IBlockAccess world, BlockPos pos) {
        return normalState.getBlock().getLightOpacity(this, world, pos);
    }

    @Override
    @Deprecated
    public int getLightValue() {
        return normalState.getBlock().getLightValue(this);
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        return normalState.getBlock().getLightValue(this, world, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isTranslucent() {
        return normalState.getBlock().isTranslucent(this);
    }

    @Override
    public boolean useNeighborBrightness() {
        return normalState.getBlock().getUseNeighborBrightness(this);
    }

    @Override
    public MapColor getMapColor(IBlockAccess p_185909_1_, BlockPos p_185909_2_) {
        return normalState.getBlock().getMapColor(this, p_185909_1_, p_185909_2_);
    }

    @Override
    public IBlockState withRotation(Rotation rot) {
        return normalState.getBlock().withRotation(this, rot);
    }

    @Override
    public IBlockState withMirror(Mirror mirrorIn) {
        return normalState.getBlock().withMirror(this, mirrorIn);
    }

    @Override
    public boolean isFullCube() {
        return normalState.getBlock().isFullCube(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasCustomBreakingProgress() {
        return normalState.getBlock().hasCustomBreakingProgress(this);
    }

    @Override
    public EnumBlockRenderType getRenderType() {
        return normalState.getBlock().getRenderType(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockAccess source, BlockPos pos) {
        return normalState.getBlock().getPackedLightmapCoords(this, source, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue() {
        return normalState.getBlock().getAmbientOcclusionLightValue(this);
    }

    @Override
    public boolean isBlockNormalCube() {
        return normalState.getBlock().isBlockNormalCube(this);
    }

    @Override
    public boolean isNormalCube() {
        return normalState.getBlock().isNormalCube(this);
    }

    @Override
    public boolean canProvidePower() {
        return normalState.getBlock().canProvidePower(this);
    }

    @Override
    public int getWeakPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return normalState.getBlock().getWeakPower(this, blockAccess, pos, side);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return normalState.getBlock().hasComparatorInputOverride(this);
    }

    @Override
    public int getComparatorInputOverride(World worldIn, BlockPos pos) {
        return normalState.getBlock().getComparatorInputOverride(this, worldIn, pos);
    }

    @Override
    public float getBlockHardness(World worldIn, BlockPos pos) {
        return normalState.getBlock().getBlockHardness(this, worldIn, pos);
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World worldIn, BlockPos pos) {
        return normalState.getBlock().getPlayerRelativeBlockHardness(this, player, worldIn, pos);
    }

    @Override
    public int getStrongPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return normalState.getBlock().getStrongPower(this, blockAccess, pos, side);
    }

    @Override
    public EnumPushReaction getMobilityFlag() {
        return normalState.getMobilityFlag();
    }

    @Override
    public IBlockState getActualState(IBlockAccess blockAccess, BlockPos pos) {
        return normalState.getBlock().getActualState(this, blockAccess, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos) {
        return normalState.getBlock().getSelectedBoundingBox(this, worldIn, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing) {
        return normalState.getBlock().shouldSideBeRendered(this, blockAccess, pos, facing);
    }

    @Override
    public boolean isOpaqueCube() {
        return normalState.getBlock().isOpaqueCube(this);
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockAccess worldIn, BlockPos pos) {
        return normalState.getBlock().getCollisionBoundingBox(this, worldIn, pos);
    }

    @Override
    public void addCollisionBoxToList(World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185908_6_) {
        normalState.getBlock().addCollisionBoxToList(this, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185908_6_);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockAccess blockAccess, BlockPos pos) {
        return normalState.getBlock().getBoundingBox(this, blockAccess, pos);
    }

    @Override
    public RayTraceResult collisionRayTrace(World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        return normalState.getBlock().collisionRayTrace(this, worldIn, pos, start, end);
    }

    @Override
    @Deprecated
    public boolean isTopSolid() {
        return normalState.getBlock().isTopSolid(this);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return normalState.getBlock().doesSideBlockRendering(this, world, pos, side);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return normalState.getBlock().isSideSolid(this, world, pos, side);
    }

    @Override
    public boolean doesSideBlockChestOpening(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return normalState.getBlock().doesSideBlockChestOpening(this, world, pos, side);
    }

    @Override
    public Vec3d getOffset(IBlockAccess access, BlockPos pos) {
        return normalState.getBlock().getOffset(this, access, pos);
    }

    @Override
    public boolean causesSuffocation() {
        return normalState.getBlock().causesSuffocation(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockPos pos, EnumFacing facing) {
        return normalState.getBlock().getBlockFaceShape(worldIn, this, pos, facing);
    }
    // </editor-fold>
}
