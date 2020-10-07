package org.dimdev.vanillafix.bugs.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends EntityPlayer {
    @Shadow public abstract void handleFalling(double y, boolean onGroundIn);

    public MixinEntityPlayerMP(World worldIn, GameProfile gameProfileIn) {super(worldIn, gameProfileIn);}

    /**
     * @reason Entities should not be invulnerable after dimension change, players could
     * intentionally abuse this. isInvulnerableDimensionChange is now only to prevent
     * teleporting the player again (in the other nether portal before the client had the
     * time to confirm the teleport).
     */
    @Redirect(method = "isEntityInvulnerable", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isEntityInvulnerable(Lnet/minecraft/util/DamageSource;)Z"))
    private boolean isEntityInvulnerable(EntityPlayer entityPlayer, DamageSource source) {
        return false;
    }

    /**
     * @reason This is incorrectly set to 1, but not noticable in vanilla since the move logic
     * trusts the client about its y position after a move due to a bug: (y > -0.5 || y
     * < 0.5) rather than &&. If this is fixed, a player standing in moving water at the
     * edge of a block is considered to have "moved wrongly" and teleported onto the block.
     * <p>
     * Leaving this to 1 would also allow hacked clients to step up blocks without having
     * to jump (not increasing hunger).
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void afterInit(MinecraftServer server, WorldServer worldIn, GameProfile profile, PlayerInteractionManager interactionManagerIn, CallbackInfo ci) {
        stepHeight = 0.7F;
    }

//    /**
//     * @reason Anti-cheat: Calculate fall damage even if not receiving move packets from the client.
//     * Otherwise, the client could disconnect without sending move packets to avoid fall damage.
//     *
//     * Also simplifies fall logic a lot.
//     */
//    @Inject(method = "updateFallState", at = @At("HEAD"))
//    public void updateFallState(double y, boolean onGround, IBlockState state, BlockPos pos, CallbackInfo ci) {
//        handleFalling(y, this.onGround);
//    }
}
