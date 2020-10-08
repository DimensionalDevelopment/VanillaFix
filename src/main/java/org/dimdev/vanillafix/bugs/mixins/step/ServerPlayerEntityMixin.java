package org.dimdev.vanillafix.bugs.mixins.step;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    /**
     * @reason This is incorrectly set to 1, but not noticeable in vanilla since the move logic
     * trusts the client about its y position after a move due to a bug: (y > -0.5 || y
     * < 0.5) rather than &&. If this is fixed, a player standing in moving water at the
     * edge of a block is considered to have "moved wrongly" and teleported onto the block.
     * <p>
     * Leaving this to 1 would also allow hacked clients to step up blocks without having
     * to jump (not increasing hunger).
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterInit(MinecraftServer server, ServerWorld world, GameProfile profile, ServerPlayerInteractionManager interactionManager, CallbackInfo ci) {
        this.stepHeight = 0.7F;
    }
}
