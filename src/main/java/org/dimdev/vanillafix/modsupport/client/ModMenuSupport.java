package org.dimdev.vanillafix.modsupport.client;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import org.dimdev.vanillafix.util.config.ModConfig;

public class ModMenuSupport implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> AutoConfig.getConfigScreen(ModConfig.class, screen).get();
    }
}
