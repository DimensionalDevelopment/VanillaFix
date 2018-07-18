package org.dimdev.vanillafix.dynamicresources.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DynamicBakedModelProvider implements IRegistry<ModelResourceLocation, IBakedModel> {
    private static final Logger LOGGER = LogManager.getLogger();
    public static DynamicBakedModelProvider instance;

    private final IRegistry<ResourceLocation, IModel> modelProvider;
    private final Map<ModelResourceLocation, IBakedModel> permanentlyLoadedBakedModels = new HashMap<>();
    private final Cache<ModelResourceLocation, Optional<IBakedModel>> loadedBakedModels =
            CacheBuilder.newBuilder()
                        .expireAfterAccess(3, TimeUnit.MINUTES)
                        .maximumSize(1000)
                        .concurrencyLevel(8)
                        .softValues()
                        .build();

    public DynamicBakedModelProvider(DynamicModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }

    @Override
    @Nullable
    public IBakedModel getObject(ModelResourceLocation location) {
        try {
            return loadedBakedModels.get(location, () -> Optional.ofNullable(loadBakedModel(location))).orElse(null);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private IBakedModel loadBakedModel(ModelResourceLocation location) {
//        LOGGER.info("Loading baked model " + location);

        IBakedModel bakedModel = permanentlyLoadedBakedModels.get(location);
        if (bakedModel != null) {
            return bakedModel;
        }

        try {
            ResourceLocation inventoryVariantLocation = ModelLocationInformation.getInventoryVariantLocation(location);
            if (inventoryVariantLocation != null) {
                IModel model = modelProvider.getObject(inventoryVariantLocation);

                if (model instanceof VanillaModelWrapper) {
                    for (ResourceLocation dep : ((VanillaModelWrapper) model).getOverrides()) {
                        if (!location.equals(dep)) {
                            ModelLocationInformation.addInventoryVariantLocation(ModelLocationInformation.getInventoryVariant(dep.toString()), dep);
                        }
                    }
                }

                return model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            }

            IModel model = modelProvider.getObject(location);
            return model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK, ModelLoader.defaultTextureGetter());
        } catch (Throwable t) {
            LOGGER.error("Error occured while loading model {}", location, t);
        }

        return null;
    }

    @Override
    public void putObject(ModelResourceLocation key, IBakedModel value) {
        permanentlyLoadedBakedModels.put(key, value);
        loadedBakedModels.invalidate(key);
    }

    @Override
    public Set<ModelResourceLocation> getKeys() {
        return permanentlyLoadedBakedModels.keySet();
    }

    @Override
    public Iterator<IBakedModel> iterator() {
        return permanentlyLoadedBakedModels.values().iterator();
    }
}
