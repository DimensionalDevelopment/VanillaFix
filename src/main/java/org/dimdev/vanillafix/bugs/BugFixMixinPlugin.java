package org.dimdev.vanillafix.bugs;

import java.util.List;
import java.util.Set;

import org.dimdev.vanillafix.VanillaFix;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class BugFixMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    // TODO: make this better
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        switch (mixinClassName) {
            case "org.dimdev.vanillafix.bugs.mixins.step.ServerPlayerEntityMixin":
                return VanillaFix.config().antiCheat.fixStepHeight;
            case "org.dimdev.vanillafix.bugs.mixins.EntityMixin":
                return VanillaFix.config().bugFixes.updateFallDistance;
            case "org.dimdev.vanillafix.bugs.mixins.MinecraftServerMixin":
                return VanillaFix.config().bugFixes.disableInitialChunkLoad;
            case "org.dimdev.vanillafix.bugs.mixins.PlayerInventoryMixin":
                return VanillaFix.config().bugFixes.fixRecipeBookIngredientsWithTags;
            case "org.dimdev.vanillafix.bugs.mixins.invulnerable.ServerPlayerEntityMixin":
                return VanillaFix.config().antiCheat.noPlayerInvulnerabilityAfterTeleport;
            case "org.dimdev.vanillafix.bugs.mixins.client.MinecraftClientMixin":
                return VanillaFix.config().clientOnly.splitScreenAndTextureProfiler;
            case "org.dimdev.vanillafix.bugs.mixins.client.ClientPlayerEntityMixin":
                return VanillaFix.config().clientOnly.screenInNetherPortal;
            case "org.dimdev.vanillafix.bugs.mixins.client.ClientPlayNetworkHandlerMixin":
                return VanillaFix.config().clientOnly.fastInterdimensionalTeleportation;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
