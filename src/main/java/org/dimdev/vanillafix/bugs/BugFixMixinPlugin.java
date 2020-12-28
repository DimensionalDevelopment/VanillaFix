package org.dimdev.vanillafix.bugs;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.dimdev.vanillafix.VanillaFix;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import static org.dimdev.vanillafix.VanillaFix.config;

public class BugFixMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return Optional.ofNullable(VanillaFix.MIXIN_CONFIGS.get(targetClassName)).map(pair -> {
			try {
				Object e = VanillaFix.config()
						.getClass()
						.getField(pair.getCategory())
						.get(VanillaFix.config());
				return e.getClass().getField(pair.getValue()).getBoolean(e);
			} catch (IllegalAccessException | NoSuchFieldException ignored) {
				throw new AssertionError();
			}
		}).orElse(Boolean.TRUE);
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
