package org.dimdev.vanillafix.dynamicresources.mixins.client;

import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelDynBucket;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.vanillafix.dynamicresources.DynamicTextureMap;
import org.dimdev.vanillafix.dynamicresources.model.DynamicBakedModelProvider;
import org.dimdev.vanillafix.dynamicresources.model.DynamicModelProvider;
import org.dimdev.vanillafix.dynamicresources.model.ModelLocationInformation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.util.Set;

@Mixin(ModelManager.class)
public class MixinModelManager {
    @Shadow private IRegistry<ModelResourceLocation, IBakedModel> modelRegistry;
    @Shadow private IBakedModel defaultModel;
    @Shadow @Final private BlockModelShapes modelProvider;
    @Shadow @Final private TextureMap texMap;

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * @reason Don't set up the ModelLoader. Instead, set up the caching DynamicModelProvider
     * and DynamicBakedModelProviders, which will act as the model registry.
     */
    @Overwrite
    public void onResourceManagerReload(IResourceManager resourceManager) {
        // Generate information about model locations, such as the blockstate location to block map
        // and the item variant to model location map.
        ModelLocationInformation.init(modelProvider.getBlockStateMapper());

        // Get custom loaders
        Set<ICustomModelLoader> loaders;
        try {
            Field loadersField = ModelLoaderRegistry.class.getDeclaredField("loaders");
            loadersField.setAccessible(true);
            // noinspection unchecked
            loaders = (Set<ICustomModelLoader>) loadersField.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        // Create the dynamic model and bake model providers
        DynamicModelProvider dynamicModelProvider = new DynamicModelProvider(loaders);
        DynamicModelProvider.instance = dynamicModelProvider;
        DynamicBakedModelProvider dynamicBakedModelProvider = new DynamicBakedModelProvider(dynamicModelProvider);
        DynamicBakedModelProvider.instance = dynamicBakedModelProvider;
        modelRegistry = dynamicBakedModelProvider;

        // Create the texture map
        ((DynamicTextureMap) texMap).init();

        // Get the default model, returned by getModel when the model provider returns null
        defaultModel = modelRegistry.getObject(new ModelResourceLocation("builtin/missing", "missing"));

        // Register the universal bucket item
        if(FluidRegistry.isUniversalBucketEnabled()) {
            ModelLoader.setBucketModelDefinition(ForgeModContainer.getInstance().universalBucket);
        }
        ModelDynBucket.LoaderDynBucket.INSTANCE.register(texMap);

        // Post the event, but just log an error if a listener throws an exception. The ModelLoader is
        // null, but very few mods use it. Custom support will be needed for those that do.
        postEventAllowingErrors(new ModelBakeEvent((ModelManager) (Object) this, modelRegistry, null));

        // Make the model provider load blockstate to model information. See MixinBlockModelShapes
        modelProvider.reloadModels();
    }

    private static void postEventAllowingErrors(Event event) {
        int busID;
        try {
            Field busIDField = EventBus.class.getDeclaredField("busID");
            busIDField.setAccessible(true);
            busID = (int) busIDField.get(MinecraftForge.EVENT_BUS);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        IEventListener[] listeners = event.getListenerList().getListeners(busID);
        for (IEventListener listener : listeners) {
            try {
                listener.invoke(event);
            } catch (Throwable t) {
                LOGGER.error(event + " listener " + listener + "threw exception, models may be broken", t);
            }
        }
    }
}
