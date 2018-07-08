package org.dimdev.utils;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class ProfileFinder {
    private static File getMinecraftJarFile() {
        try {
            URL url = Launch.classLoader.getResource("net/minecraft/client/main/Main.class");
            if (url.getProtocol().equals("jar")) url = new URL(url.getFile().substring(0, url.getFile().indexOf('!')));
            return new File(url.toURI());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getCurrentMinecraftProfileFile() {
        File minecraftJarFile = getMinecraftJarFile();
        File profileFile = new File(minecraftJarFile.getParentFile(), minecraftJarFile.getParentFile().getName() + ".json");
        return profileFile.exists() ? profileFile : null;
    }

    public static File getCurrentForgeProfileFile() {
        File versionsDir = getMinecraftJarFile().getParentFile().getParentFile();
        String versionId = ForgeVersion.mcVersion + "-" + ForgeVersion.MOD_ID + ForgeVersion.mcVersion + "-" + ForgeVersion.getVersion();
        File profileFile = new File(versionsDir, versionId + File.separatorChar + versionId + ".json");
        System.out.println(profileFile);
        return profileFile.exists() ? profileFile : null;
    }

    public static File getGameDir() {
        return getMinecraftJarFile().getParentFile().getParentFile().getParentFile();
    }
}
