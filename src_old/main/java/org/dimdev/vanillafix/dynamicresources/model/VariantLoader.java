package org.dimdev.vanillafix.dynamicresources.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class VariantLoader implements ICustomModelLoader {
    public static final VariantLoader INSTANCE = new VariantLoader();
    private Cache<ResourceLocation, ModelBlockDefinition> modelBlockDefinitionCache =
            CacheBuilder.newBuilder()
                        .expireAfterAccess(2, TimeUnit.MINUTES)
                        .maximumSize(100)
                        .concurrencyLevel(8)
                        .softValues()
                        .build();

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation instanceof ModelResourceLocation;
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        ModelResourceLocation variant = (ModelResourceLocation) modelLocation;
        ModelBlockDefinition definition = getModelBlockDefinition(variant);

        if (definition.hasVariant(variant.getVariant())) {
            return new WeightedRandomModel(definition.getVariant(variant.getVariant()));
        } else {
            if (definition.hasMultipartData()) {
                Block block = ModelLocationInformation.getBlockFromBlockstateLocation(new ResourceLocation(variant.getNamespace(), variant.getPath()));
                if (block != null) {
                    definition.getMultipartData().setStateContainer(block.getBlockState());
                }
            }

            return new MultipartModel(new ResourceLocation(variant.getNamespace(), variant.getPath()), definition.getMultipartData());
        }
    }

    private ModelBlockDefinition getModelBlockDefinition(ResourceLocation location) {
        ResourceLocation simpleLocation = new ResourceLocation(location.getNamespace(), location.getPath());
        try {
            return modelBlockDefinitionCache.get(simpleLocation, () -> loadModelBlockDefinition(simpleLocation));
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private ModelBlockDefinition loadModelBlockDefinition(ResourceLocation location) {
        ResourceLocation blockstateLocation = new ResourceLocation(location.getNamespace(), "blockstates/" + location.getPath() + ".json");

        List<ModelBlockDefinition> list = Lists.newArrayList();
        try {
            for (IResource resource : Minecraft.getMinecraft().getResourceManager().getAllResources(blockstateLocation)) {
                list.add(loadModelBlockDefinition(location, resource));
            }
        } catch (IOException e) {
            throw new RuntimeException("Encountered an exception when loading model definition of model " + blockstateLocation, e);
        }

        return new ModelBlockDefinition(list);
    }

    private ModelBlockDefinition loadModelBlockDefinition(ResourceLocation location, IResource resource) {
        ModelBlockDefinition definition;

        try (InputStream is = resource.getInputStream()) {
            definition = ModelBlockDefinition.parseFromReader(new InputStreamReader(is, StandardCharsets.UTF_8), location);
        } catch (Exception exception) {
            throw new RuntimeException("Encountered an exception when loading model definition of '" + location + "' from: '" + resource.getResourceLocation() + "' in resourcepack: '" + resource.getResourcePackName() + "'", exception);
        }

        return definition;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public String toString() {
        return "VariantLoader";
    }
}
