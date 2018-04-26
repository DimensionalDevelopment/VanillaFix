package org.dimdev.vanillafix.mixins;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({"unused", "NonConstantFieldWithUpperCaseName"}) // Shadow
@Mixin(NetHandlerPlayServer.class)
public abstract class MixinNetHandlerPlayServer implements INetHandlerPlayServer {

    @Shadow public EntityPlayerMP player;
    @Shadow @Final private MinecraftServer server;
    @Shadow private int networkTickCount;
    @Shadow private double firstGoodX;
    @Shadow private double firstGoodY;
    @Shadow private double firstGoodZ;
    @Shadow private double lastGoodX;
    @Shadow private double lastGoodY;
    @Shadow private double lastGoodZ;
    @Shadow private int lastPositionUpdate;
    @Shadow private boolean floating;
    @Shadow private Vec3d targetPos;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int movePacketCounter;
    @Shadow private int lastMovePacketCounter;
    @Shadow private int teleportId;

    @Shadow public void disconnect(final ITextComponent textComponent) {}

    @Shadow private static boolean isMovePlayerPacketInvalid(CPacketPlayer packetIn) { return false; }

    @Shadow private void captureCurrentPosition() {}

    @Shadow public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {}

    /**
     * Rewrite buggy vanilla method:
     * <p>
     * 1. After a teleport, the player should be teleported to the correct location even if they don't
     * send a confirmation packet. This fixes MC-98153, preventing players from abusing it to teleport
     * to the nether without changing coordinates by disconnecting before sending that packet, or to remain
     * invulnerable (but unable to move) by not sending the confirmation packet. Interdimensional teleportation
     * also seems much faster now.
     * <p>
     * 2. Block collisions should not be called until the move has been confirmed to be correct (the player
     * didn't cheat). This prevents players from cheating to trigger block collision methods such as end
     * portals. Also fixes MC-123364, where if anti-cheat is triggered when walking into an end portal,
     * the position gets reverted but not the world, likely dropping the player into the void.
     * <p>
     * Bugs fixed:
     * - https://bugs.mojang.com/browse/MC-89928
     * - https://bugs.mojang.com/browse/MC-98153
     * - https://bugs.mojang.com/browse/MC-123364
     * - Movements made by server are reverted every second until client confirms teleport
     * - Movement stats get increased with illegal moves
     * - If teleport packet is received with ping > 1s, game is unplayable until player stops moving
     * - lastGoodX/Y/Z not captured after teleport, resulting in "moved too quickly" spam after the client
     * accepts a teleport in the same tick.
     * <p>
     * Improvements:
     * - Correct position rather than cause jump-backs when player moves wrongly/into a block
     */

    @Overwrite
    @Override
    public void processPlayer(CPacketPlayer packet) {
        PacketThreadUtil.checkThreadAndEnqueue(packet, this, player.getServerWorld());

        if (isMovePlayerPacketInvalid(packet)) {
            disconnect(new TextComponentTranslation("multiplayer.disconnect.invalid_player_movement"));
            return;
        }

        // Optimization: Check for queuedEndExit first, get or load the world if necessary only.
        if (player.queuedEndExit) return;

        WorldServer world = server.getWorld(player.dimension);

        if (networkTickCount == 0) {
            captureCurrentPosition();
        }

        if (targetPos != null) {
            // Fix: Vanilla did this, but why? It's impossible for a packet to get lost with TCP. If we send
            // another update packet after just one second, a one-second ping will make the game completely
            // unplayable after the server updates the player's position, since by the time the client confirms
            // a position change, the server will have sent it another one, repeatedly causing the player to jump
            // back until they stop moving for whatever the ping time is.

            /*if (networkTickCount - lastPositionUpdate > 20) {
                lastPositionUpdate = networkTickCount;

                // Fix: Vanilla code was: setPlayerLocation(targetPos.x, targetPos.y, targetPos.z,
                // player.rotationYaw, player.rotationPitch); Why would the server revert movements
                // after a teleport just because the client didn't confirm it? I'm assuming that they
                // intended to send another updated move packet:
                setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
            }*/

            // Instead, we will send another packet both here and in processConfirmTeleport if the position the client
            // was sent is no longer good (exceeds tolerance):

            if (targetPos.squareDistanceTo(player.posX, player.posY, player.posZ) > 1.0D) {
                setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
            }

            // Ignore the packet, the client thinks it's still at the old position
            return;
        }

        lastPositionUpdate = networkTickCount;

        if (player.isRiding()) {
            player.setPositionAndRotation(player.posX, player.posY, player.posZ, packet.getYaw(player.rotationYaw), packet.getPitch(player.rotationPitch));
            server.getPlayerList().serverUpdateMovingPlayer(player);
        } else {
            double prevX = player.posX;
            double prevY = player.posY;
            double prevZ = player.posZ;

            double packetX = packet.getX(player.posX);
            double packetY = packet.getY(player.posY);
            double packetZ = packet.getZ(player.posZ);
            float packetYaw = packet.getYaw(player.rotationYaw);
            float packetPitch = packet.getPitch(player.rotationPitch);

            // Calculate difference from last tick
            double xDiff = packetX - firstGoodX;
            double yDiff = packetY - firstGoodY;
            double zDiff = packetZ - firstGoodZ;
            double distanceSq = xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;

            double motionSq = player.motionX * player.motionX + player.motionY * player.motionY + player.motionZ * player.motionZ;

            if (player.isPlayerSleeping()) {
                if (distanceSq > 1.0D) {
                    // The player tried to move while sleeping. Send the player the correct position.
                    setPlayerLocation(player.posX, player.posY, player.posZ, packet.getYaw(player.rotationYaw), packet.getPitch(player.rotationPitch));
                }
            } else {
                movePacketCounter++;
                int packetCount = movePacketCounter - lastMovePacketCounter;

                if (packetCount > 5) {
                    LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", player.getName(), packetCount);
                    packetCount = 1;
                }

                // Fix: Disable isInvulnerableDimensionChange, not necessary if we don't revert position after teleport anymore
                if (/*!player.isInvulnerableDimensionChange() && */!(player.getServerWorld().getGameRules().getBoolean("disableElytraMovementCheck") && player.isElytraFlying())) {
                    // Not really anti-cheat. Max speed would be 447 blocks/second with 5 packets/tick... Also, shouldn't
                    // distance increase linearly with the packet count rather than proportional to sqrt(packetCount)?
                    float maxDistanceSqPerPacket = player.isElytraFlying() ? 300.0F : 100.0F;

                    if (distanceSq - motionSq > maxDistanceSqPerPacket * packetCount/* && !(server.isSinglePlayer() && server.getServerOwner().equals(player.getName()))*/) { // TODO: Single-player server owner check disabled for testing
                        LOGGER.warn("{} moved too quickly! {},{},{}", player.getName(), xDiff, yDiff, zDiff);
                        setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                        return;
                    }
                }

                boolean wasInsideBlock = !world.getCollisionBoxes(player, player.getEntityBoundingBox().shrink(0.0625D)).isEmpty();

                // Calculate difference from last packet
                xDiff = packetX - lastGoodX;
                yDiff = packetY - lastGoodY;
                zDiff = packetZ - lastGoodZ;

                if (player.onGround && !packet.isOnGround() && yDiff > 0.0D) {
                    player.jump();
                }

                // Move the player towards the desired position, but not passing through solid
                // blocks, past block edges while sneaking, or climbing stairs that are too tall.
                // TODO: Players could cheat to avoid collisions with non-solid blocks (end portal), fix this.
                player.move(MoverType.PLAYER, xDiff, yDiff, zDiff);
                player.onGround = packet.isOnGround();

                // Fix: If a collision teleported the player, just sync the client without checking that the move was legal.
                // Entity.move already made sure that the player didn't cheat, and reverting the move would be wrong because
                // the prevX/Y/Z is no longer good in the new dimension. This fixes MC-123364.
                if (player.isInvulnerableDimensionChange()) {
                    // A better name for invulnerableDimensionChange would be "lastBlockCollisionCausedPlayerMove". See
                    // https://github.com/ModCoderPack/MCPBot-Issues/issues/624. This happens when the move caused a
                    // collision that teleported the player elsewhere.

                    // Fix: Immediately set the correct position after a teleport and, rather than reverting it and waiting
                    // for the player to confirm the teleport (which they might never do). This fixes MC-98153.
                    setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

                    // Fix: Disable invulnerability after teleport, this could be abused by players, allowing them to stay
                    // invulnerable but unable to move after a teleport.
                    player.clearInvulnerableDimensionChange();

                    // Fix: In processConfirmTeleport (which we moved here, since we're doing the move immediately), only
                    // lastGoodX/Y/Z are set to the current position, but firstGoodX/Y/Z should be updated too (this can
                    // be done using captureCurrentPosition). Otherwise, this would result in "moved wrongly" messages if
                    // the client both accepts the teleport and starts sending move packets that are correct within the same tick.
                    captureCurrentPosition();

                    yDiff = 0; // Reset fall distance
                } else {
                    // Calculate difference from position accepted by Entity.move
                    xDiff = packetX - player.posX;
                    yDiff = packetY - player.posY;
                    zDiff = packetZ - player.posZ;

                    double yDiffA = yDiff > -0.5D || yDiff < 0.5D ? 0.0D : yDiff;
                    distanceSq = xDiff * xDiff + yDiffA * yDiffA + zDiff * zDiff;
                    boolean movedWrongly = false;
                    // Optimization: Remove !player.isPlayerSleeping() check, that was already checked
                    if (distanceSq > 0.0625D /*&& !player.isPlayerSleeping()*/ && !player.interactionManager.isCreative() && player.interactionManager.getGameType() != GameType.SPECTATOR) {
                        movedWrongly = true;
                        LOGGER.warn("{} moved wrongly!", player.getName());
                    }

                    boolean wouldMoveInsideBlock = !world.getCollisionBoxes(player, player.getEntityBoundingBox().offset(xDiff, yDiff, zDiff).shrink(0.0625D)).isEmpty();
                    if (!movedWrongly && !wouldMoveInsideBlock || wasInsideBlock || player.noClip) {
                        // If the position the player wanted is close enough to what Entity.move calculated
                        // and not inside a block, or the player was noclipping (either because player.noClip
                        // was true or they were inside a block), use the client's position instead
                        player.setPositionAndRotation(packetX, packetY, packetZ, packetYaw, packetPitch);
                    } else {
                        // Improvement: Instead of reverting the move to prevX/Y/Z, accept the corrected move, since the
                        // Entity.move method made sure that it was legal. This has the advantage of just correcting the
                        // position rather than causing jump-backs and is safer against in case a block collision forgets
                        // to set invulnerableDimensionChange after a teleport (only causes a log message and some extra
                        // unnecessary checks).
                        setPlayerLocation(player.posX, player.posY, player.posZ, packetYaw, packetPitch);
                        player.addMovementStat(player.posX - prevX, player.posY - prevY, player.posZ - prevZ);
                        return;
                    }
                    // Fix: Update movement stats to the corrected position rather than the position the client wanted.
                    // This prevents illegal, cancelled (vanilla) or corrected (with improvement above), moves from updating
                    // stats.
                    player.addMovementStat(player.posX - prevX, player.posY - prevY, player.posZ - prevZ);
                }

                floating = yDiff >= -0.03125D;
                floating &= !server.isFlightAllowed() && !player.capabilities.allowFlying;
                floating &= !player.isPotionActive(MobEffects.LEVITATION) && !player.isElytraFlying() && !world.checkBlockCollision(player.getEntityBoundingBox().grow(0.0625D).expand(0.0D, -0.55D, 0.0D));
                player.onGround = packet.isOnGround();
                server.getPlayerList().serverUpdateMovingPlayer(player);
                player.handleFalling(player.posY - prevY, packet.isOnGround());

                lastGoodX = player.posX;
                lastGoodY = player.posY;
                lastGoodZ = player.posZ;
            }
        }
    }

    /**
     * Update this method for changes made above.
     * <p>
     * Bugs fixed:
     * - Movements made by server are reverted when client confirms teleport
     */
    @Overwrite
    @Override
    public void processConfirmTeleport(CPacketConfirmTeleport packet) {
        PacketThreadUtil.checkThreadAndEnqueue(packet, this, player.getServerWorld());

        if (packet.getTeleportId() == teleportId) {
            if (targetPos.squareDistanceTo(player.posX, player.posY, player.posZ) > 1.0D) {
                // The client accepted the position change, but it's too late, something moved the player. Sync it again.
                setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
            } else {
                targetPos = null;
            }
        }
    }
}
