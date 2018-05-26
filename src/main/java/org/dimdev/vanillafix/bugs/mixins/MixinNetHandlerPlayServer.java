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
import java.util.Set;

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
    @Shadow public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {}

    private static final boolean SKIP_ANTICHEAT = true; // TODO
    private static final boolean DEBUG_SPEED_BUFFER = false;
    private static final double EXCESS_SPEED_BUFFER = 3; // blocks (balance between resync packets and client desyncs)
    private static final double SPEED_MULTIPLIER = 0.11785905161311232 / 0.1;

    private double horizontalDistance = 0;
    private double verticalDistance = 0;
    private double queuedX = 0;
    private double queuedY = 0;
    private double queuedZ = 0;
    private long lastTickTime = -1;
    private double tickLag = 1;

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
        player.onGround = !player.world.getCollisionBoxes(player, player.getEntityBoundingBox().expand(0, -0.05, 0)).isEmpty();
        if (player.onGround && !packet.isOnGround() && yDiff > 0.0D) {
            player.jump();
        }

        boolean trustPlayer = SKIP_ANTICHEAT || server.isSinglePlayer() && server.getServerOwner().equals(player.getName());
        if (trustPlayer) {
            double prevX = player.posX;
            double prevY = player.posY;
            double prevZ = player.posZ;
            player.move(MoverType.PLAYER, xDiff, yDiff, zDiff);
            player.setPositionAndRotation(packetX, packetY, packetZ, packetYaw, packetPitch);
            player.addMovementStat(player.posX - prevX, player.posY - prevY, player.posZ - prevZ);
            server.getPlayerList().serverUpdateMovingPlayer(player);
            return;
        }

        // TODO: Fix everything below this line

        if (!packet.isOnGround() && !player.capabilities.isFlying) {
            yDiff = 0;
        }

        // Check that a movement with a change in y is valid
        if (yDiff < 0.01 && yDiff > -0.01) yDiff = 0; // Just caused by client/server not being in perfect sync, ignore
        if (yDiff != 0 && !player.capabilities.isFlying) {
            if (yDiff > player.stepHeight || yDiff < -player.stepHeight) {
                LOGGER.warn("{} tried to step over a step too high! yDiff = {}", player.getName(), yDiff);
                yDiff = 0;
            } else if (player.onGround) {
                AxisAlignedBB newBoundingBox = player.getEntityBoundingBox().offset(xDiff, yDiff, zDiff);
                boolean targetOnGround = !player.world.getCollisionBoxes(player, newBoundingBox.expand(0, -0.05, 0)).isEmpty();
                if (!targetOnGround) {
                    LOGGER.warn("{} tried to fly! yDiff = {}", player.getName(), yDiff);
                    yDiff = 0;
                }
                // TODO: Make sure the player didn't walk over gaps
            } else {
                if (yDiff < -1 || yDiff > 1) {
                    LOGGER.warn("{} tried to control y position while falling! yDiff = {}", player.getName(), yDiff);
                }
                yDiff = 0;
            }
        }

        // Check that max movement speed is being respected
        double hDiff = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double vDiff = yDiff > 0 ? yDiff : -yDiff;
        double maxSpeed = SPEED_MULTIPLIER * tickLag * (player.capabilities.isFlying ? 3 * player.capabilities.getFlySpeed() : player.capabilities.getWalkSpeed());
        if (player.isSneaking()) maxSpeed *= 0.3;
        if (player.isSprinting()) maxSpeed *= 1.3;

        if (horizontalDistance + hDiff > maxSpeed) {
            boolean buffer;
            if (horizontalDistance + hDiff - maxSpeed > EXCESS_SPEED_BUFFER) {
                LOGGER.warn("{} moved too quickly! hDiff = {}, maxSpeed = {}", player.getName(), hDiff, maxSpeed);
                syncClientPosition();
                return;
            } else {
                if (DEBUG_SPEED_BUFFER) LOGGER.info("{} has excess speed, buffering! horizontalDistance = {}, maxSpeed = {}", player.getName(), hDiff, maxSpeed);
                buffer = true;
            }
            double prevXDiff = xDiff;
            double prevZDiff = zDiff;

            hDiff = maxSpeed - horizontalDistance;

            double angle = Math.atan2(zDiff, xDiff);
            xDiff = hDiff * Math.cos(angle);
            zDiff = hDiff * Math.sin(angle);

            if (buffer) { // TODO: queue the actual movement path, not just displacement
                queuedX = prevXDiff - xDiff;
                queuedZ = prevZDiff - zDiff;
            }
        } else {
            queuedX = 0;
            queuedZ = 0;
        }

        horizontalDistance += hDiff;

        // TODO: check vertical speed

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
        double xError = packetX - (player.posX + queuedX);
        double yError = packetY - (player.posY + queuedY);
        double zError = packetZ - (player.posZ + queuedZ);
        double errorSq = xError * xError /*+ yError * yError */ + zError * zError;  // TODO: Figure out why y-desync happens (client-side bug)

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
        if (errorSq > 0.0625D || yError > 10 || yError < -10) {
            LOGGER.warn("{} out of sync, resyncing! error = {}", player.getName(), Math.sqrt(errorSq));
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
            syncClientPosition();
//        } else if (errorSq > 0.0625D) {
//            LOGGER.warn("{} moved wrongly! error = {}", player.getName(), Math.sqrt(errorSq));
//            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
        } else if (movedIntoBlock) {
            LOGGER.warn("{} tried to move into a block!", player.getName());
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
            syncClientPosition();
        } else {
            // TODO: Isn't the tolerance of 0.25 blocks too big? Couldn't the player pass through a glass pane?
            //player.setPositionAndRotation(prevX + xDiff, prevY + yDiff, prevZ + zDiff, packetYaw, packetPitch);
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
        }

        // Add movement stats
        player.addMovementStat(player.posX - prevX, player.posY - prevY, player.posZ - prevZ);

        server.getPlayerList().serverUpdateMovingPlayer(player);
    }

    /** Resyncs the client's position (but not rotation, to make it less annoying for the player) **/
    private void syncClientPosition() {
        queuedX = queuedY = queuedZ = 0;
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

    @Inject(method = "setPlayerLocation(DDDFFLjava/util/Set;)V", at = @At("HEAD"))
    private void resetQueuedMovement(double x, double y, double z, float yaw, float pitch, Set<SPacketPlayerPosLook.EnumFlags> relativeSet, CallbackInfo ci) {
        if (!relativeSet.contains(SPacketPlayerPosLook.EnumFlags.X)) queuedX = 0;
        if (!relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Y)) queuedY = 0;
        if (!relativeSet.contains(SPacketPlayerPosLook.EnumFlags.Z)) queuedZ = 0;
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

    /**
     * @reason Reset distance travelled in a tick every tick. A teleport does not
     * reset this, and this is intended.
     */
    @Inject(method = "update", at = @At("HEAD"))
    private void resetDistanceTravelled(CallbackInfo ci) {
        long time = System.currentTimeMillis();
        if (lastTickTime != -1) tickLag = ((time - lastTickTime) / 1000D) * 20;
        lastTickTime = time;

        horizontalDistance = 0;
        verticalDistance = 0;
        if (queuedX != 0 || queuedY != 0 || queuedZ != 0) {
            // TODO: don't make a fake packet, instead split processPlayer into two methods
            processPlayer(new CPacketPlayer.Position(player.posX + queuedX, player.posY + queuedY, player.posZ + queuedZ, player.onGround));
            if (DEBUG_SPEED_BUFFER && queuedX == 0 && queuedY == 0 && queuedZ == 0) {
                LOGGER.info("{}'s speed buffer cleared!", player.getName());
            }
        }
    }
}
