package org.dimdev.vanillafix.mixins;

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

@Mixin(World.class)
public abstract class MixinWorld { // TODO: prefix modid for classes too
    @Shadow @Final public Profiler profiler;

    @Shadow public abstract void updateEntity(Entity ent);

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void entityOnUpdate(Entity entity) {
        profiler.func_194340_a(() -> {
            final ResourceLocation entityID = EntityList.getKey(entity);
            return entityID == null ? entity.getClass().getSimpleName() : entityID.toString();
        });
        entity.onUpdate();
        profiler.endSection();
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V"))
    public void updateEntity(World world, Entity entity) {
        profiler.func_194340_a(() -> {
            final ResourceLocation entityID = EntityList.getKey(entity);
            return entityID == null ? entity.getClass().getSimpleName() : entityID.toString();
        });
        updateEntity(entity);
        profiler.endSection();
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    public void entityOnUpdate(ITickable tileEntity) {
        profiler.func_194340_a(() -> {
            final ResourceLocation tileEntityID = TileEntity.getKey(((TileEntity) tileEntity).getClass());
            return tileEntityID == null ? tileEntity.getClass().getSimpleName() : tileEntityID.toString();
        });
        tileEntity.update();
        profiler.endSection();
    }
}
