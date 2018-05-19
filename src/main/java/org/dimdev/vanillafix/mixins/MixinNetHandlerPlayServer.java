package org.dimdev.vanillafix.mixins;

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

@Mixin(value = NetHandlerPlayServer.class, priority = 500) // After sponge
public abstract class MixinNetHandlerPlayServer implements INetHandlerPlayServer {

    @Shadow public EntityPlayerMP player;
    @Shadow @Final private MinecraftServer server;
    @Shadow private int networkTickCount;
    @Shadow private double lastGoodX;
    @Shadow private double lastGoodY;
    @Shadow private double lastGoodZ;
    @Shadow private Vec3d targetPos;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int teleportId;

    @Shadow public void disconnect(final ITextComponent textComponent) {}
    @Shadow private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) { return false; }
    @Shadow private void captureCurrentPosition() {}
    @Shadow public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {}

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
     */
    @Overwrite
    @Override
    public void processPlayer(CPacketPlayer packet) {
        //if (true) return;
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

        // Noclip/spectator players can move around freely in the world
        if (player.noClip) {
            player.setPositionAndRotation(packetX, packetY, packetZ, packetYaw, packetPitch);
            server.getPlayerList().serverUpdateMovingPlayer(player);
            return;
        }

        // Handle jump
        if (player.onGround && !packet.isOnGround() && yDiff > 0.0D) {
            player.jump();
        }

        // TODO: Check that velocity, max speed, and gravity are respected

        // Store info about the old position
        double prevX = player.posX;
        double prevY = player.posY;
        double prevZ = player.posZ;
        List<AxisAlignedBB> oldCollisonBoxes = player.world.getCollisionBoxes(player, player.getEntityBoundingBox().shrink(0.0625D));

        // Move the player towards the position they want
        player.move(MoverType.PLAYER, packetX - player.posX, packetY - player.posY, packetZ - player.posZ);

        // Move caused a teleport, capture new position, and stop processing packet
        if (targetPos != null) {
            captureCurrentPosition();
            server.getPlayerList().serverUpdateMovingPlayer(player);
            return;
        }

        xDiff = packetX - player.posX;
        yDiff = packetY - player.posY;
        zDiff = packetZ - player.posZ;
        diffSq = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;

        // The player should be allowed to move out of blocks, but not into blocks other than the
        // ones they're currently in.
        List<AxisAlignedBB> newCollisonBoxes = player.world.getCollisionBoxes(player, player.getEntityBoundingBox().offset(xDiff, yDiff, zDiff).shrink(0.0625D));
        boolean movedIntoBlock = false;
        for (AxisAlignedBB collisionBox : newCollisonBoxes) {
            if (!oldCollisonBoxes.contains(collisionBox)) {
                movedIntoBlock = true;
                break;
            }
        }

        // Accept the packet's position if it's close enough (0.25 blocks) and not in a new block
        if (diffSq > 0.0625D) {
            LOGGER.warn("{} moved wrongly!", player.getName());
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
            syncClientPosition();
        } else if (movedIntoBlock) {
            LOGGER.warn("{} moved into a block!", player.getName());
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
            syncClientPosition();
        } else {
            player.setPositionAndRotation(packetX, packetY, packetZ, packetYaw, packetPitch);
        }

        // Add movement stats
        player.addMovementStat(player.posX - prevX, player.posY - prevY, player.posZ - prevZ);

        server.getPlayerList().serverUpdateMovingPlayer(player);

        lastGoodX = player.posX;
        lastGoodY = player.posY;
        lastGoodZ = player.posZ;
    }

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
