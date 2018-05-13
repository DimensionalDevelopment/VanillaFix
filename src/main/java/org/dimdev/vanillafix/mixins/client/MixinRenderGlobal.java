package org.dimdev.vanillafix.mixins.client;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal { // TODO: prefix modid for classes too
    @Shadow private WorldClient world;

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntityStatic(Lnet/minecraft/entity/Entity;FZ)V"))
    public void renderEntityStatic(RenderManager renderManager, Entity entity, float partialTicks, boolean disableDebugBoundingBox) {
        world.profiler.func_194340_a(() -> {
            final ResourceLocation entityID = EntityList.getKey(entity);
            return entityID == null ? entity.getClass().getSimpleName() : entityID.toString();
        });
        renderManager.renderEntityStatic(entity, partialTicks, disableDebugBoundingBox);
        world.profiler.endSection();
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V"))
    public void renderEntityStatic(TileEntityRendererDispatcher renderDispatcher, TileEntity tileEntity, float partialTicks, int destroyStage) {
        world.profiler.func_194340_a(() -> {
            final ResourceLocation tileEntityID = TileEntity.getKey(((TileEntity) tileEntity).getClass());
            return tileEntityID == null ? tileEntity.getClass().getSimpleName() : tileEntityID.toString();
        });
        renderDispatcher.render(tileEntity, partialTicks, destroyStage);
        world.profiler.endSection();
    }
}
