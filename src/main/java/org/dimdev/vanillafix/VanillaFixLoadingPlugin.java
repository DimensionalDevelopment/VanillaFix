package org.dimdev.vanillafix;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.SSLUtils;
import org.dimdev.vanillafix.crashes.DeobfuscatingRewritePolicy;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import sun.misc.URLClassPath;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 10000)
public class VanillaFixLoadingPlugin implements IFMLLoadingPlugin {
    private static final Logger log = LogManager.getLogger("VanillaFix");
    public static LoadingConfig config;
    private static final boolean deobfuscatedEnvironment = VanillaFixLoadingPlugin.class.getResource("/net/minecraft/world/World.class") != null;

    static {
        log.info("Initializing VanillaFix");
        config = new LoadingConfig(new File(Launch.minecraftHome, "config/vanillafix.cfg"));
        config.improvedLaunchWrapper = false; // TODO: fix this

        replaceLaunchWrapper();
        trustIdenTrust();
        initStacktraceDeobfuscator();
        fixMixinClasspathOrder();
        initMixin();
    }

    private static void replaceLaunchWrapper() {
        if (!config.improvedLaunchWrapper) {
            log.info("LaunchWrapper replacement disabled by config");
            return;
        }

        if (deobfuscatedEnvironment) {
            log.info("Skipping LaunchWrapper replacement in dev environment");
            return;
        }

        String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();

        if (classPath.contains("launchwrapper-2.0.jar")) {
            log.info("Currently running launchwrapper 2.0");
            return;
        }

        if (!classPath.contains("launchwrapper-1.12.jar")) {
            log.info("Couldn't find launchwrapper on classpath");
            return;
        }

        // Replace the vanilla LaunchWrapper library with a more efficient fork (https://github.com/DimensionalDevelopment/LegacyLauncher)
        try {
            // Copy launchwrapper-2.0.jar library to '.minecraft/libraries'
            File launchWrapperTargetFile = LibraryManager.getDefaultRepo().getFile("net/minecraft/launchwrapper/2.0/launchwrapper-2.0.jar");
            if (!launchWrapperTargetFile.exists()) {
                launchWrapperTargetFile.getParentFile().mkdirs();
                // 'jar_' is a workaround for https://github.com/johnrengelman/shadow/issues/276
                try (InputStream is = VanillaFixLoadingPlugin.class.getResourceAsStream("/launchwrapper-2.0.jar_");
                     OutputStream os = new FileOutputStream(launchWrapperTargetFile)) {
                    IOUtils.copy(is, os);
                }
            }

            // Restart the game with the new library
            File libraryPath = new File(System.getProperty("java.library.path"));
            File newLibraryPath = Files.createTempDirectory("natives").toFile();
            FileUtils.copyDirectory(libraryPath, newLibraryPath);

            String command = makeNewCommandLine(newLibraryPath.getAbsolutePath());

            log.info("Restarting Minecraft with command: " + command);
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Method haltMethod = Class.forName("java.lang.Shutdown").getDeclaredMethod("halt", int.class);
            haltMethod.setAccessible(true);
            haltMethod.invoke(null, 0);
        } catch (Throwable t) {
            log.error("Failed to replace launchwrapper", t);
        }
    }

    private static String makeNewCommandLine(String newLibraryPath) {
        StringBuilder command = new StringBuilder();

        // Java command
        command.append(System.getProperty("java.home"))
               .append(File.separatorChar).append("bin")
               .append(File.separatorChar).append("javaw");

        // JVM args
        for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (arg.startsWith("-Djava.library.path=")) {
                arg = "-Djava.library.path=" + newLibraryPath;
            }
            command.append(" ").append(arg);
        }

        // Classpath
        command.append(" -cp ").append(StringUtils.replace(ManagementFactory.getRuntimeMXBean().getClassPath(),
                "1.12" + File.separatorChar + "launchwrapper-1.12.jar",
                "2.0" + File.separatorChar + "launchwrapper-2.0.jar"
        ));

        // Command args
        command.append(" ").append(System.getProperty("sun.java.command"));

        return command.toString();
    }

    private static void trustIdenTrust() {
        // Trust the "IdenTrust DST Root CA X3" certificate (used by Let's Encrypt, which is used by paste.dimdev.org)
        try (InputStream keyStoreInputStream = VanillaFixLoadingPlugin.class.getResourceAsStream("/dst_root_ca_x3.jks")) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, "password".toCharArray());
            SSLUtils.trustCertificates(keyStore);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException();
        }
    }

    private static void initStacktraceDeobfuscator() {
        File modDir = new File(Launch.minecraftHome, "config/vanillafix");
        modDir.mkdirs();

        // Initialize StacktraceDeobfuscator
        log.info("Initializing StacktraceDeobfuscator");
        try {
            File mappings = new File(modDir, "methods-stable_39.csv");
            if (mappings.exists()) {
                log.info("Found MCP method mappings: " + mappings.getName());
            } else {
                log.info("Downloading MCP method mappings to: " + mappings.getName());
            }
            StacktraceDeobfuscator.init(mappings);
        } catch (Exception e) {
            log.error("Failed to get MCP data!", e);
        }
        log.info("Done initializing StacktraceDeobfuscator");

        // Install the log exception deobfuscation rewrite policy
        DeobfuscatingRewritePolicy.install();
    }

    private static void fixMixinClasspathOrder() {
        // Move VanillaFix jar up in the classloader's URLs to make sure that the
        // latest version of Mixin is used (to avoid having to rename 'VanillaFix.jar'
        // to 'aaaVanillaFix.jar')
        URL url = VanillaFixLoadingPlugin.class.getProtectionDomain().getCodeSource().getLocation();
        givePriorityInClasspath(url, Launch.classLoader);
        givePriorityInClasspath(url, (URLClassLoader) ClassLoader.getSystemClassLoader());
    }

    private static void givePriorityInClasspath(URL url, URLClassLoader classLoader) {
        try {
            Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
            ucpField.setAccessible(true);

            List<URL> urls = new ArrayList<>(Arrays.asList(classLoader.getURLs()));
            urls.remove(url);
            urls.add(0, url);
            URLClassPath ucp = new URLClassPath(urls.toArray(new URL[0]));

            ucpField.set(classLoader, ucp);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static void initMixin() {
        MixinBootstrap.init();

        if (config.bugFixes) Mixins.addConfiguration("mixins.vanillafix.bugs.json");
        if (config.crashFixes) Mixins.addConfiguration("mixins.vanillafix.crashes.json");
        if (config.profiler) Mixins.addConfiguration("mixins.vanillafix.profiler.json");
        if (config.textureFixes) Mixins.addConfiguration("mixins.vanillafix.textures.json");
        if (config.modSupport) Mixins.addConfiguration("mixins.vanillafix.modsupport.json");
        if (config.blockstates) Mixins.addConfiguration("mixins.vanillafix.blockstates.json");
        if (config.dynamicResources) Mixins.addConfiguration("mixins.vanillafix.dynamicresources.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
