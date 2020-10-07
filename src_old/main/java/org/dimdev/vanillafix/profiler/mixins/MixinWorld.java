package org.dimdev.vanillafix.profiler.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Since Sponge effectively disables the Minecraft profiler, there's no
 * point in trying to make this mixin compatible with Sponge. Therefore,
 * we only apply this mixin when Sponge is not installed
 */
@Mixin(World.class)
public abstract class MixinWorld { // TODO: prefix modid for classes too
    @Shadow @Final public Profiler profiler;

    @Shadow public abstract void updateEntity(Entity ent);

    /**
     * @reason Adds subsections to the "root.tick.level.entities.regular.tick"
     * profiler, using the entity ID, or the class name if the ID is null.
     */
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    private void entityOnUpdate(Entity entity) {
        profiler.func_194340_a(() -> { // func_194340_a = startSection(Supplier<String>)
            final ResourceLocation entityID = EntityList.getKey(entity);
            return entityID == null ? entity.getClass().getSimpleName() : entityID.toString();
        });
        entity.onUpdate();
        profiler.endSection();
    }

    /**
     * @reason Adds subsections to the "root.tick.level.entities.regular.tick"
     * profiler, using the entity ID, or the class name if the ID is null.
     */
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V"))
    private void updateEntity(World world, Entity entity) {
        profiler.func_194340_a(() -> { // func_194340_a = startSection(Supplier<String>)
            final ResourceLocation entityID = EntityList.getKey(entity);
            return entityID == null ? entity.getClass().getSimpleName() : entityID.toString();
        });
        updateEntity(entity);
        profiler.endSection();
    }

    /**
     * @reason Adds subsections to the "root.tick.level.entities.blockEntities"
     * profiler, using the entity ID, or the class name if the ID is null.
     */
    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    private void tileEntityUpdate(ITickable tileEntity) {
        profiler.func_194340_a(() -> { // func_194340_a = startSection(Supplier<String>)
            final ResourceLocation tileEntityID = TileEntity.getKey(((TileEntity) tileEntity).getClass());
            return tileEntityID == null ? tileEntity.getClass().getSimpleName() : tileEntityID.toString();
        });
        tileEntity.update();
        profiler.endSection();
    }
}
