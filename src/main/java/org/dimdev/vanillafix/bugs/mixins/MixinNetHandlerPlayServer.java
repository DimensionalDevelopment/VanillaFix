package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumSet;
import java.util.List;

@Mixin(value = NetHandlerPlayServer.class, priority = 500) // Sponge
public abstract class MixinNetHandlerPlayServer implements INetHandlerPlayServer {

    @Shadow public EntityPlayerMP player;
    @Shadow(aliases = "serverController") @Final private MinecraftServer server;
    @Shadow private int networkTickCount;
    @Shadow private Vec3d targetPos;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int teleportId;

    @Shadow public void disconnect(final ITextComponent textComponent) {}
    @Shadow private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) { return false; }
    @Shadow private void captureCurrentPosition() {}

    /**
     * @reason See https://github.com/DimensionalDevelopment/VanillaFix/wiki/Move-Logic-Rewrite
     * <p>
     * Bugs fixed:
     * - https://bugs.mojang.com/browse/MC-89928
     * - https://bugs.mojang.com/browse/MC-98153
     * - https://bugs.mojang.com/browse/MC-123364
     * - Movements made by server are reverted every second until client confirms teleport
     * - Movement stats get increased with illegal moves
     * - If teleport packet is received with ping > 1s, game is unplayable until player stops moving
     * - lastGoodX/Y/Z not captured after teleport
     * - Redundant code in processPlayer
     * - Implement a better anticheat system (don't trust the client about anything)
     * - TODO: Walking against flowing water does not add the correct movement stats
     */
    @Overwrite
    @Override
    public void processPlayer(CPacketPlayer packet) {
        PacketThreadUtil.checkThreadAndEnqueue(packet, this, player.getServerWorld());

        // Disconnect the player if packet is invalid (doubles are infinite or out of range)
        if (isMovePlayerPacketInvalid(packet)) {
            disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_player_movement"));
            return;
        }

        // Player hasn't confirmed a teleport or respawned after end exit
        if (targetPos != null || player.queuedEndExit) {
            return;
        }

        // In case a move packet is received before first NetHandlerPlayServer.update
        if (networkTickCount == 0) {
            captureCurrentPosition();
        }

        // Read the packet, defaulting to current x/y/z/yaw/pitch
        double packetX = packet.getX(player.posX);
        double packetY = packet.getY(player.posY);
        double packetZ = packet.getZ(player.posZ);
        float packetYaw = packet.getYaw(player.rotationYaw);
        float packetPitch = packet.getPitch(player.rotationPitch);

        // If the player is riding an entity, accept yaw/pitch but not position
        if (player.isRiding()) {
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
            server.getPlayerList().serverUpdateMovingPlayer(player);
            return;
        }

        double xDiff = packetX - player.posX;
        double yDiff = packetY - player.posY;
        double zDiff = packetZ - player.posZ;
        double diffSq = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;

        // Move packet received while sleeping, client somehow forgot it was sleeping or server missed
        // wake up packet.
        if (player.isPlayerSleeping()) {
            if (diffSq < 1) return;
            LOGGER.warn("{} tried to move while sleeping! {}, {}, {}", player.getName(), xDiff, yDiff, zDiff);
            player.wakeUpPlayer(false, true, true);
        }

        // Noclip (including spectator mode) players can move around freely in the world
        if (player.noClip) {
            player.setPositionAndRotation(packetX, packetY, packetZ, packetYaw, packetPitch);
            server.getPlayerList().serverUpdateMovingPlayer(player);
            return;
        }

        // Handle jump
        if (player.onGround && !packet.isOnGround() && yDiff > 0.0D) {
            player.jump();
            player.posY += 0.5;
        }

        if (!player.capabilities.isFlying && !player.isOnLadder() && !player.isInWater() && !player.onGround && !packet.isOnGround() && player.motionY <= -0.5) {
            yDiff = 0;
        }
        if (yDiff > 0) {
            player.fallDistance = 0;
        }

        // Store info about the old position
        double prevX = player.posX;
        double prevY = player.posY;
        double prevZ = player.posZ;
        List<AxisAlignedBB> oldCollisonBoxes = player.world.getCollisionBoxes(player, player.getEntityBoundingBox().shrink(0.0625D));

        // Move the player towards the position they want
        player.move(MoverType.PLAYER, xDiff, yDiff, zDiff);

        // Move caused a teleport, capture new position, and stop processing packet
        if (targetPos != null) {
            captureCurrentPosition();
            server.getPlayerList().serverUpdateMovingPlayer(player);
            return;
        }

        // Check if we need to resync the player (they moved wrongly), or accept the packet's position
        double xError = packetX - player.posX;
        double yError = packetY - player.posY;
        double zError = packetZ - player.posZ;
        double errorSq = xError * xError + zError * zError; // TODO

        // The player should be allowed to move out of blocks, but not into blocks other than the
        // ones they're currently in.
        List<AxisAlignedBB> newCollisonBoxes = player.world.getCollisionBoxes(player, player.getEntityBoundingBox().offset(
                prevX + xDiff - player.posX, prevY + yDiff - player.posY, prevZ + zDiff - player.posZ).shrink(0.0625D));
        boolean movedIntoBlock = false;
        for (AxisAlignedBB collisionBox : newCollisonBoxes) {
            if (!oldCollisonBoxes.contains(collisionBox)) {
                movedIntoBlock = true;
                break;
            }
        }

        // Accept the packet's position if it's close enough (0.25 blocks) and not in a new block
        if (errorSq > 0.0625D /*|| yError > 5 || yError < -5*/) {
            LOGGER.warn("{} out of sync, resyncing! error = {}", player.getName(), Math.sqrt(errorSq));
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
            syncClientPosition();
        } else if (movedIntoBlock) {
            LOGGER.warn("{} tried to move into a block!", player.getName());
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
            syncClientPosition();
        } else {
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
        }

        // Add movement stats
        player.addMovementStat(player.posX - prevX, player.posY - prevY, player.posZ - prevZ);

        server.getPlayerList().serverUpdateMovingPlayer(player);
    }

    /** Resyncs the client's position (but not rotation, to make it less annoying for the player) **/
    private void syncClientPosition() {
        targetPos = new Vec3d(player.posX, player.posY, player.posZ);
        player.connection.sendPacket(new SPacketPlayerPosLook(
                player.posX,
                player.posY,
                player.posZ,
                0,
                0,
                EnumSet.of(SPacketPlayerPosLook.EnumFlags.X_ROT, SPacketPlayerPosLook.EnumFlags.Y_ROT),
                ++teleportId == Integer.MAX_VALUE ? 0 : teleportId));
    }

    /**
     * @reason Update this method for changes made above.
     * <p>
     * Bugs fixed:
     * - Movements made by server are reverted when client confirms teleport
     */
    @Overwrite
    @Override
    public void processConfirmTeleport(CPacketConfirmTeleport packet) {
        PacketThreadUtil.checkThreadAndEnqueue(packet, this, player.getServerWorld());

        if (packet.getTeleportId() == teleportId) {
            targetPos = null;
            player.clearInvulnerableDimensionChange(); // Allow portal timer to decrease
        }
    }

    /**
     * @reason Capture position after entity update, not before (teleport player immediately to correct
     * position after dimesion change).
     * <p>
     * Bugs fixed:
     * - https://bugs.mojang.com/browse/MC-98153
     */
    @Inject(method = "update", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/entity/player/EntityPlayerMP;onUpdateEntity()V", ordinal = 0))
    private void afterUpdateEntity(CallbackInfo ci) {
        captureCurrentPosition();
    }
}
