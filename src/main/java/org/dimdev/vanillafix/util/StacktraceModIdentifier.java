package org.dimdev.vanillafix.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.launch.knot.Knot;

public class StacktraceModIdentifier {
    private static final Logger LOGGER = LogManager.getLogger();

    private StacktraceModIdentifier() {
    }

    public static Set<ModContainer> identifyFromStacktrace(Throwable e) {
        Map<Path, Set<ModContainer>> modMap = makeModMap();

        // Get the set of classes
        HashSet<String> classes = new LinkedHashSet<>();
        while (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                classes.add(element.getClassName());
            }
            e = e.getCause();
        }

        Set<ModContainer> mods = new LinkedHashSet<>();
        for (String className : classes) {
            Set<ModContainer> classMods = identifyFromClass(className, modMap);
            if (classMods != null) mods.addAll(classMods);
        }
        return mods;
    }

    public static Set<ModContainer> identifyFromClass(String className) {
        return identifyFromClass(className, makeModMap());
    }

    private static Set<ModContainer> identifyFromClass(String className, Map<Path, Set<ModContainer>> modMap) {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) {
            return Collections.emptySet();
        }

        // Get the URL of the class
        URL url = Knot.getLauncher().getTargetClassLoader().getResource(className.replace('.', '/') + ".class");
        LOGGER.debug(className + " = " + className + " = " + url);
        if (url == null) {
            LOGGER.warn("Failed to identify " + className);
            return Collections.emptySet();
        }

        // Get the mod containing that class
        try {
            if (url.getProtocol().equals("jar")) url = new URL(url.getFile().substring(0, url.getFile().indexOf('!')));
            return modMap.get(Paths.get(url.toURI()));
        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Path, Set<ModContainer>> makeModMap() {
        Map<Path, Set<ModContainer>> modMap = new HashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            Set<ModContainer> currentMods = modMap.getOrDefault(mod.getPath(mod.getRootPath().toString()), new HashSet<>());
            currentMods.add(mod);
            modMap.put(mod.getPath(mod.getRootPath().toString()), currentMods);
        }
        return modMap;
    }
}
