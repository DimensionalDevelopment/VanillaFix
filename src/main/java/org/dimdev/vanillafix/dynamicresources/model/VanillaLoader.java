package org.dimdev.vanillafix.dynamicresources.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class VanillaLoader implements ICustomModelLoader {
    public static final VanillaLoader INSTANCE = new VanillaLoader();

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return true;
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        // Load vanilla model
        ModelBlock vanillaModel;
        ResourceLocation vanillaModelLocation = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath() + ".json");
        try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(vanillaModelLocation);
             Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            vanillaModel = ModelBlock.deserialize(reader);
            vanillaModel.name = modelLocation.toString();
        }

        // Load armature animation (currently disabled for efficiency, see MixinModelBlockAnimation)
        String modelPath = modelLocation.getPath();
        if (modelPath.startsWith("models/")) {
            modelPath = modelPath.substring("models/".length());
        }
        ResourceLocation armatureLocation = new ResourceLocation(modelLocation.getNamespace(), "armatures/" + modelPath + ".json");
        ModelBlockAnimation animation = ModelBlockAnimation.loadVanillaAnimation(Minecraft.getMinecraft().getResourceManager(), armatureLocation);

        // Return the vanilla model weapped in a VanillaModelWrapper
        return new VanillaModelWrapper(modelLocation, vanillaModel , false, animation);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public String toString() {
        return "VanillaLoader";
    }
}
