package org.dimdev.utils;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ModIdentifier { // TODO: non-forge mods too
    private static final Logger log = LogManager.getLogger();

    public static Set<ModContainer> identifyFromStacktrace(Throwable e) {
        Map<File, Set<ModContainer>> modMap = makeModMap();

        HashSet<String> classes = new HashSet<>();
        while (e != null) {
            for (StackTraceElement element : e.getStackTrace()) {
                classes.add(element.getClassName());
            }
            e = e.getCause();
        }

        Set<ModContainer> mods = new HashSet<>();
        for (String className : classes) {
            Set<ModContainer> classMods = identifyFromClass(className, modMap);
            if (classMods != null) mods.addAll(classMods);
        }
        return mods;
    }

    public static Set<ModContainer> identifyFromClass(String className) {
        return identifyFromClass(className, makeModMap());
    }

    private static Set<ModContainer> identifyFromClass(String className, Map<File, Set<ModContainer>> modMap) {
        final String untrasformedName = untransformName(Launch.classLoader, className);
        URL url = Launch.classLoader.getResource(untrasformedName.replace('.', '/') + ".class");
        log.debug(className + " = " + untrasformedName + " = " + url);
        if (url == null) {
            log.warn("Failed to identify " + className + " (untransformed name: " + untrasformedName + ")");
            return new HashSet<>();
        }
        String str = url.getFile();
        if (str.startsWith("file:/")) str = str.substring(str.indexOf("/") + 1); // jar:file:/
        if (str.contains("!")) str = str.substring(0, str.indexOf("!"));
        return modMap.get(new File(str));
    }

    private static Map<File, Set<ModContainer>> makeModMap() {
        Map<File, Set<ModContainer>> modMap = new HashMap<>();
        for (ModContainer mod : Loader.instance().getModList()) {
            Set<ModContainer> currentMods = modMap.getOrDefault(mod.getSource(), new HashSet<>());
            currentMods.add(mod);
            modMap.put(mod.getSource(), currentMods);
        }

        try {
            modMap.remove(Loader.instance().getMinecraftModContainer().getSource()); // Ignore minecraft jar (minecraft)
            modMap.remove(Loader.instance().getIndexedModList().get("FML").getSource()); // Ignore forge jar (FML, forge)
        } catch (NullPointerException ignored) {}

        return modMap;
    }

    private static String untransformName(LaunchClassLoader launchClassLoader, String className) {
        try {
            Method untransformNameMethod = LaunchClassLoader.class.getDeclaredMethod("untransformName", String.class);
            untransformNameMethod.setAccessible(true);
            return (String) untransformNameMethod.invoke(launchClassLoader, className);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
