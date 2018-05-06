package org.dimdev.utils;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ModIdentifier { // TODO: non-forge mods too
    // private static Logger log = LogManager.getLogger();

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
        for (String class0 : classes) {
            Set<ModContainer> classMods = identifyFromClass(class0, modMap);
            if (classMods != null) mods.addAll(classMods);
        }
        return mods;
    }

    public static Set<ModContainer> identifyFromClass(String class0) {
        return identifyFromClass(class0, makeModMap());
    }

    private static Set<ModContainer> identifyFromClass(String class0, Map<File, Set<ModContainer>> modMap) {
        URL url = ModIdentifier.class.getResource('/' + class0.replace('.', '/') + ".class");
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
        return modMap;
    }
}
