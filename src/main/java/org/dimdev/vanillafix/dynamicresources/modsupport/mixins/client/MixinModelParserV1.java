package org.dimdev.vanillafix.dynamicresources.modsupport.mixins.client;

import net.minecraftforge.client.model.ICustomModelLoader;
import org.dimdev.vanillafix.dynamicresources.model.VanillaLoader;
import org.spongepowered.asm.mixin.*;
import team.chisel.ctm.client.model.parsing.ModelParserV1;

@Pseudo
@Mixin(ModelParserV1.class)
public class MixinModelParserV1 {
    @SuppressWarnings("unused") @Shadow(remap = false) @Mutable @Final private static ICustomModelLoader VANILLA_LOADER = VanillaLoader.INSTANCE;
}
