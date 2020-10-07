package org.dimdev.vanillafix.profiler.mixins.client;

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

    /**
     * @reason Adds subsections to the "root.gameRenderer.level.entities.entities"
     * profiler, using the entity ID, or the class name if the ID is null.
     */
    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntityStatic(Lnet/minecraft/entity/Entity;FZ)V"))
    private void renderEntityStatic(RenderManager renderManager, Entity entity, float partialTicks, boolean disableDebugBoundingBox) {
        world.profiler.func_194340_a(() -> { // func_194340_a = startSection(Supplier<String>)
            final ResourceLocation entityId = EntityList.getKey(entity);
            return entityId == null ? entity.getClass().getSimpleName() : entityId.toString();
        });
        renderManager.renderEntityStatic(entity, partialTicks, disableDebugBoundingBox);
        world.profiler.endSection();
    }

    /**
     * @reason Adds subsections to the "root.gameRenderer.level.entities.blockentities"
     * profiler, using the tile entity ID, or the class name if the id is null.
     */
    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V"))
    private void tileEntityRender(TileEntityRendererDispatcher renderDispatcher, TileEntity tileEntity, float partialTicks, int destroyStage) {
        world.profiler.func_194340_a(() -> { // func_194340_a = startSection(Supplier<String>)
            final ResourceLocation tileEntityId = TileEntity.getKey(((TileEntity) tileEntity).getClass());
            return tileEntityId == null ? tileEntity.getClass().getSimpleName() : tileEntityId.toString();
        });
        renderDispatcher.render(tileEntity, partialTicks, destroyStage);
        world.profiler.endSection();
    }
}
