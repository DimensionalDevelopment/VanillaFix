package org.dimdev.vanillafix.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({"unused", "NonConstantFieldWithUpperCaseName"}) // Shadow
@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient implements INetHandlerPlayClient {
    @Shadow private boolean doneLoadingTerrain;
    @Shadow private WorldClient world;
    @Shadow private Minecraft client;

    @Overwrite
    @Override
    public void handleRespawn(SPacketRespawn packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, client);

        if (packetIn.getDimensionID() != client.player.dimension) {
            doneLoadingTerrain = false;
            client.displayGuiScreen(null);

            Scoreboard scoreboard = world.getScoreboard();
            world = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, client.world.getWorldInfo().isHardcoreModeEnabled(), packetIn.getWorldType()), packetIn.getDimensionID(), packetIn.getDifficulty(), client.mcProfiler);
            world.setWorldScoreboard(scoreboard);
            client.loadWorld(world);
            client.player.dimension = packetIn.getDimensionID();
        }

        client.setDimensionAndSpawnPlayer(packetIn.getDimensionID());
        client.playerController.setGameType(packetIn.getGameType());
    }

//    @Redirect(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V", ordinal = 0))
//    public void patchDisplayGuiScreen(Minecraft minecraft, GuiScreen screen) {}
//
//    @Inject(method = "handleRespawn", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/NetHandlerPlayClient;doneLoadingTerrain:Z", ordinal = 0))
//    public void setDoneLoadingTerrain(boolean value) {
//        client.displayGuiScreen(null);
//    }
}
