package org.dimdev.vanillafix.dynamicresources.model;

import com.google.common.collect.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.animation.AnimationItemOverrideList;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.ITransformation;
import net.minecraftforge.common.model.Models;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.common.model.animation.IClip;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * Wraps a vanilla ModelBlock to make it a Forge IModel.
 * <p>
 * Based on Forge's {@link ModelLoader.VanillaModelWrapper}.
 */
public class VanillaModelWrapper implements IModel {
    public static final ModelBlockAnimation DEFAULT_MODEL_BLOCK_ANIMATION = new ModelBlockAnimation(ImmutableMap.of(), ImmutableMap.of());
    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private final ResourceLocation location;
    private final ModelBlock model;
    private final boolean uvlock;
    private final ModelBlockAnimation animation;

    public VanillaModelWrapper(ResourceLocation location, ModelBlock model) {
        this(location, model, false, DEFAULT_MODEL_BLOCK_ANIMATION);
    }

    public VanillaModelWrapper(ResourceLocation location, ModelBlock model, boolean uvlock, ModelBlockAnimation animation) {
        this.location = location;
        this.model = model;
        this.uvlock = uvlock;
        this.animation = animation;
    }

    private static boolean hasItemModel(ModelBlock model) {
        return model.getRootModel() == BuiltinLoader.MODEL_GENERATED;
    }

    private static boolean isCustomRenderer(ModelBlock model) {
        return model.getRootModel() == BuiltinLoader.MODEL_ENTITY;
    }

    public static BakedQuad makeBakedQuad(BlockPart blockPart, BlockPartFace blockPartFace, TextureAtlasSprite sprite, EnumFacing face, ITransformation transform, boolean uvLocked) {
        return FACE_BAKERY.makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, blockPartFace, sprite, face, transform, blockPart.partRotation, uvLocked, blockPart.shade);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        Set<ResourceLocation> set = Sets.newHashSet();
        for (ResourceLocation dep : model.getOverrideLocations()) {
            if (!location.equals(dep)) {
                set.add(dep);
            }
        }
        if (model.getParentLocation() != null && !model.getParentLocation().getPath().startsWith("builtin/")) {
            set.add(model.getParentLocation());
        }
        return ImmutableSet.copyOf(set);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        // Setting parent here to make textures resolve properly
        if (model.getParentLocation() != null) {
            if (model.getParentLocation().getPath().equals("builtin/generated")) {
                model.parent = BuiltinLoader.MODEL_GENERATED;
            } else {
                IModel parent = ModelLoaderRegistry.getModelOrLogError(model.getParentLocation(), "Could not load vanilla model parent '" + model.getParentLocation() + "' for '" + model);
                if (parent instanceof VanillaModelWrapper) {
                    model.parent = ((VanillaModelWrapper) parent).model;
                } else {
                    throw new IllegalStateException("Vanilla model '" + model + "' can't have non-vanilla parent");
                }
            }
        }

        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

        if (hasItemModel(model)) {
            for (String s : ItemModelGenerator.LAYERS) {
                String r = model.resolveTextureName(s);
                ResourceLocation loc = new ResourceLocation(r);
                if (!r.equals(s)) {
                    builder.add(loc);
                }
            }
        }
        for (String s : model.textures.values()) {
            if (!s.startsWith("#")) {
                builder.add(new ResourceLocation(s));
            }
        }
        return builder.build();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if (!Attributes.moreSpecific(format, Attributes.DEFAULT_BAKED_FORMAT)) {
            throw new IllegalArgumentException("can't bake vanilla models to the format that doesn't fit into the default one: " + format);
        }
        ModelBlock model = this.model;

        if (model == null) {
            return BuiltinLoader.WRAPPED_MODEL_MISSING.bake(BuiltinLoader.WRAPPED_MODEL_MISSING.getDefaultState(), format, bakedTextureGetter);
        }

        List<TRSRTransformation> newTransforms = Lists.newArrayList();
        for (int i = 0; i < model.getElements().size(); i++) {
            BlockPart part = model.getElements().get(i);
            newTransforms.add(animation.getPartTransform(state, part, i));
        }

        ItemCameraTransforms transforms = model.getAllTransforms();
        Map<ItemCameraTransforms.TransformType, TRSRTransformation> tMap = Maps.newEnumMap(ItemCameraTransforms.TransformType.class);
        tMap.putAll(PerspectiveMapWrapper.getTransforms(transforms));
        tMap.putAll(PerspectiveMapWrapper.getTransforms(state));
        IModelState perState = new SimpleModelState(ImmutableMap.copyOf(tMap));

        if (hasItemModel(model)) {
            return new ItemLayerModel(model).bake(perState, format, bakedTextureGetter);
        }

        if (isCustomRenderer(model)) {
            return new BuiltInModel(transforms, model.createOverrides());
        }

        return bakeNormal(model, perState, state, newTransforms, format, bakedTextureGetter, uvlock);
    }

    public Collection<ResourceLocation> getOverrides() {
        return model.getOverrideLocations();
    }

    private IBakedModel bakeNormal(ModelBlock model, IModelState perState, final IModelState modelState, List<TRSRTransformation> newTransforms, final VertexFormat format, final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, boolean uvLocked) {
        final TRSRTransformation baseState = modelState.apply(Optional.empty()).orElse(TRSRTransformation.identity());
        TextureAtlasSprite particle = bakedTextureGetter.apply(new ResourceLocation(model.resolveTextureName("particle")));
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(model, model.createOverrides()).setTexture(particle);
        for (int i = 0; i < model.getElements().size(); i++) {
            if (modelState.apply(Optional.of(Models.getHiddenModelPart(ImmutableList.of(Integer.toString(i))))).isPresent()) {
                continue;
            }
            BlockPart part = model.getElements().get(i);
            TRSRTransformation transformation = baseState;
            if (newTransforms.get(i) != null) {
                transformation = transformation.compose(newTransforms.get(i));
                BlockPartRotation rot = part.partRotation;
                if (rot == null) rot = new BlockPartRotation(new org.lwjgl.util.vector.Vector3f(), EnumFacing.Axis.Y, 0, false);
                part = new BlockPart(part.positionFrom, part.positionTo, part.mapFaces, rot, part.shade);
            }
            for (Map.Entry<EnumFacing, BlockPartFace> e : part.mapFaces.entrySet()) {
                TextureAtlasSprite textureatlassprite1 = bakedTextureGetter.apply(new ResourceLocation(model.resolveTextureName(e.getValue().texture)));

                if (e.getValue().cullFace == null || !TRSRTransformation.isInteger(transformation.getMatrix())) {
                    builder.addGeneralQuad(makeBakedQuad(part, e.getValue(), textureatlassprite1, e.getKey(), transformation, uvLocked));
                } else {
                    builder.addFaceQuad(baseState.rotate(e.getValue().cullFace), makeBakedQuad(part, e.getValue(), textureatlassprite1, e.getKey(), transformation, uvLocked));
                }
            }
        }

        return new PerspectiveMapWrapper(builder.makeBakedModel(), perState) {
            private final ItemOverrideList overrides = new AnimationItemOverrideList(VanillaModelWrapper.this, modelState, format, bakedTextureGetter, super.getOverrides());

            @Override
            public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
                List<BakedQuad> quads = null;

                if (state instanceof IExtendedBlockState) {
                    IExtendedBlockState exState = (IExtendedBlockState) state;
                    if (exState.getUnlistedNames().contains(Properties.AnimationProperty)) {
                        IModelState newState = exState.getValue(Properties.AnimationProperty);
                        IExtendedBlockState newExState = exState.withProperty(Properties.AnimationProperty, null);
                        if (newState != null) {
                            quads = bake(new ModelStateComposition(modelState, newState), format, bakedTextureGetter).getQuads(newExState, side, rand);
                        }
                    }
                }

                if (quads == null) {
                    quads = super.getQuads(state, side, rand);
                }

                return quads;
            }

            @Override
            public ItemOverrideList getOverrides() {
                return overrides;
            }
        };
    }

    @Override
    public VanillaModelWrapper retexture(ImmutableMap<String, String> textures) {
        if (textures.isEmpty())
            return this;

        List<BlockPart> elements = Lists.newArrayList(); //We have to duplicate this so we can edit it below.
        for (BlockPart part : model.getElements()) {
            elements.add(new BlockPart(part.positionFrom, part.positionTo, Maps.newHashMap(part.mapFaces), part.partRotation, part.shade));
        }

        ModelBlock newModel = new ModelBlock(model.getParentLocation(), elements,
                Maps.newHashMap(model.textures), model.isAmbientOcclusion(), model.isGui3d(),
                model.getAllTransforms(), Lists.newArrayList(model.getOverrides()));
        newModel.name = model.name;
        newModel.parent = model.parent;

        Set<String> removed = Sets.newHashSet();

        for (Map.Entry<String, String> e : textures.entrySet()) {
            if ("".equals(e.getValue())) {
                removed.add(e.getKey());
                newModel.textures.remove(e.getKey());
            } else
                newModel.textures.put(e.getKey(), e.getValue());
        }

        // Map the model's texture references as if it was the parent of a model with the retexture map as its textures.
        Map<String, String> remapped = Maps.newHashMap();

        for (Map.Entry<String, String> e : newModel.textures.entrySet()) {
            if (e.getValue().startsWith("#")) {
                String key = e.getValue().substring(1);
                if (newModel.textures.containsKey(key))
                    remapped.put(e.getKey(), newModel.textures.get(key));
            }
        }

        newModel.textures.putAll(remapped);

        //Remove any faces that use a null texture, this is for performance reasons, also allows some cool layering stuff.
        for (BlockPart part : newModel.getElements()) {
            part.mapFaces.entrySet().removeIf(entry -> removed.contains(entry.getValue().texture));
        }

        return new VanillaModelWrapper(location, newModel, uvlock, animation);
    }

    @Override
    public Optional<? extends IClip> getClip(String name) {
        if (animation.getClips().containsKey(name)) {
            return Optional.ofNullable(animation.getClips().get(name));
        }

        return Optional.empty();
    }

    @Override
    public VanillaModelWrapper smoothLighting(boolean value) {
        if (model.ambientOcclusion == value) {
            return this;
        }

        ModelBlock newModel = new ModelBlock(model.getParentLocation(), model.getElements(), model.textures, value, model.isGui3d(), model.getAllTransforms(), Lists.newArrayList(model.getOverrides()));
        newModel.parent = model.parent;
        newModel.name = model.name;
        return new VanillaModelWrapper(location, newModel, uvlock, animation);
    }

    @Override
    public VanillaModelWrapper gui3d(boolean value) {
        if (model.isGui3d() == value) {
            return this;
        }

        ModelBlock newModel = new ModelBlock(model.getParentLocation(), model.getElements(), model.textures, model.ambientOcclusion, value, model.getAllTransforms(), Lists.newArrayList(model.getOverrides()));
        newModel.parent = model.parent;
        newModel.name = model.name;
        return new VanillaModelWrapper(location, newModel, uvlock, animation);
    }

    @Override
    public IModel uvlock(boolean value) {
        if (uvlock == value) {
            return this;
        }

        return new VanillaModelWrapper(location, model, value, animation);
    }
}
