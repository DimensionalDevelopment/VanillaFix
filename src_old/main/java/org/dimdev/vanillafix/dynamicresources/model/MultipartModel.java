package org.dimdev.vanillafix.dynamicresources.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.MultipartBakedModel;
import net.minecraft.client.renderer.block.model.multipart.Multipart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.Map;
import java.util.function.Function;

/**
 * A multipart model.
 * <p>
 * Based on Forge's {@link net.minecraftforge.client.model.ModelLoader.MultipartModel}.
 */
public class MultipartModel implements IModel {
    private final ResourceLocation location;
    private final Multipart multipart;
    private final ImmutableMap<Selector, IModel> partModels;

    public MultipartModel(ResourceLocation location, Multipart multipart) throws Exception {
        this.location = location;
        this.multipart = multipart;
        ImmutableMap.Builder<Selector, IModel> builder = ImmutableMap.builder();
        for (Selector selector : multipart.getSelectors()) {
            builder.put(selector, new WeightedRandomModel(selector.getVariantList()));
        }
        partModels = builder.build();
    }

    private MultipartModel(ResourceLocation location, Multipart multipart, ImmutableMap<Selector, IModel> partModels) {
        this.location = location;
        this.multipart = multipart;
        this.partModels = partModels;
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        MultipartBakedModel.Builder builder = new MultipartBakedModel.Builder();

        for (Selector selector : multipart.getSelectors()) {
            builder.putModel(selector.getPredicate(multipart.getStateContainer()), partModels.get(selector).bake(partModels.get(selector).getDefaultState(), format, bakedTextureGetter));
        }

        return builder.makeMultipartModel();
    }

    @Override
    public IModel retexture(ImmutableMap<String, String> textures) {
        if (textures.isEmpty())
            return this;

        ImmutableMap.Builder<Selector, IModel> builder = ImmutableMap.builder();
        for (Map.Entry<Selector, IModel> partModel : partModels.entrySet()) {
            builder.put(partModel.getKey(), partModel.getValue().retexture(textures));
        }

        return new MultipartModel(location, multipart, builder.build());
    }
}
