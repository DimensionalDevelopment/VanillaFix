package org.dimdev.vanillafix.dynamicresources.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantList;
import net.minecraft.client.renderer.block.model.WeightedBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * A weighted random model.
 * <p>
 * Based on Forge's {@link ModelLoader.WeightedRandomModel}.
 */
public class WeightedRandomModel implements IModel {
    private final List<Variant> variants;
    private final List<ResourceLocation> locations;
    private final Set<ResourceLocation> textures;
    private final List<IModel> models;
    private final IModelState defaultState;

    public WeightedRandomModel(VariantList variants) throws Exception {
        this.variants = variants.getVariantList();
        locations = new ArrayList<>();
        textures = Sets.newHashSet();
        models = new ArrayList<>();
        ImmutableList.Builder<Pair<IModel, IModelState>> builder = ImmutableList.builder();
        for (Variant variant : this.variants) {
            ResourceLocation location = variant.getModelLocation();
            locations.add(location);

            IModel model = variant.process(ModelLoaderRegistry.getModel(location));

            textures.addAll(model.getTextures());
            models.add(model);

            IModelState modelDefaultState = model.getDefaultState();
            Preconditions.checkNotNull(modelDefaultState, "Model %s returned null as default state", location);
            builder.add(Pair.of(model, new ModelStateComposition(variant.getState(), modelDefaultState)));
        }

        // If all variants are missing, add one with the missing model and default rotation
        if (models.size() == 0) {
            IModel missing = ModelLoaderRegistry.getMissingModel();
            models.add(missing);
            builder.add(Pair.of(missing, TRSRTransformation.identity()));
        }

        defaultState = new MultiModelState(builder.build());
    }

    private WeightedRandomModel(List<Variant> variants, List<ResourceLocation> locations, Set<ResourceLocation> textures, List<IModel> models, IModelState defaultState) {
        this.variants = variants;
        this.locations = locations;
        this.textures = textures;
        this.models = models;
        this.defaultState = defaultState;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.copyOf(locations);
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return ImmutableSet.copyOf(textures);
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if (!Attributes.moreSpecific(format, Attributes.DEFAULT_BAKED_FORMAT)) {
            throw new IllegalArgumentException("can't bake vanilla weighted models to the format that doesn't fit into the default one: " + format);
        }
        if (variants.size() == 1) {
            IModel model = models.get(0);
            return model.bake(MultiModelState.getPartState(state, model, 0), format, bakedTextureGetter);
        }
        WeightedBakedModel.Builder builder = new WeightedBakedModel.Builder();
        for (int i = 0; i < variants.size(); i++) {
            IModel model = models.get(i);
            builder.add(model.bake(MultiModelState.getPartState(state, model, i), format, bakedTextureGetter), variants.get(i).getWeight());
        }
        return builder.build();
    }

    @Override
    public IModelState getDefaultState() {
        return defaultState;
    }

    @Override
    public WeightedRandomModel retexture(ImmutableMap<String, String> textures) {
        if (textures.isEmpty())
            return this;

        // Rebuild the texture list taking into account new textures
        Set<ResourceLocation> modelTextures = Sets.newHashSet();

        // Recreate the MultiModelState so that the IModelState data is properly applied to the retextured model
        ImmutableList.Builder<Pair<IModel, IModelState>> builder = ImmutableList.builder();
        List<IModel> retexturedModels = Lists.newArrayList();
        for (int i = 0; i < variants.size(); i++) {
            IModel retextured = models.get(i).retexture(textures);
            modelTextures.addAll(retextured.getTextures());
            retexturedModels.add(retextured);
            builder.add(Pair.of(retextured, variants.get(i).getState()));
        }

        return new WeightedRandomModel(variants, locations, modelTextures, retexturedModels, new MultiModelState(builder.build()));
    }
}
