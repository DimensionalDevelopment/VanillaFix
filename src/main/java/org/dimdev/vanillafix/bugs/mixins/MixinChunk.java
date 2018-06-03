package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Chunk.class)
public class MixinChunk {

    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;
    @Shadow @Final private World world;

    @Inject(method = "onUnload", at = @At("HEAD"))
    public void onChunkUnload(CallbackInfo ci) {
        final List<EntityPlayer> players = new ArrayList<>();
        for (final ClassInheritanceMultiMap<Entity> classinheritancemultimap : entityLists) {
            for(final EntityPlayer player : classinheritancemultimap.getByClass(EntityPlayer.class)) {
                players.add(player);
            }
        }
        for (final EntityPlayer player : players) {
            world.updateEntityWithOptionalForce(player, false);
        }
    }

}
