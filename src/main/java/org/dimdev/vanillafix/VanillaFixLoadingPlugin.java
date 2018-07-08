package org.dimdev.vanillafix;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.ProfileFinder;
import org.dimdev.utils.SSLUtils;
import org.dimdev.vanillafix.crashes.DeobfuscatingRewritePolicy;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 10000)
public class VanillaFixLoadingPlugin implements IFMLLoadingPlugin {
    private static final Logger log = LogManager.getLogger();
    private static final String MCP_VERSION = "20180618-1.12"; // TODO: Use version for current Minecraft version!
    private static boolean initialized = false;

    public static LoadingConfig config;

    public VanillaFixLoadingPlugin() {
        initialize();

        MixinBootstrap.init();

        // @formatter:off
        if (config.bugFixes)      Mixins.addConfiguration("mixins.vanillafix.bugs.json");
        if (config.crashFixes)    Mixins.addConfiguration("mixins.vanillafix.crashes.json");
        if (config.profiler)      Mixins.addConfiguration("mixins.vanillafix.profiler.json");
        if (config.textureFixes)  Mixins.addConfiguration("mixins.vanillafix.textures.json");
        if (config.modSupport)    Mixins.addConfiguration("mixins.vanillafix.modsupport.json");
        // @formatter:on
    }

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        config = new LoadingConfig();
        config.init(new File(Launch.minecraftHome, "config/vanillafix.cfg"));

        // Replace LaunchWrapper with our fork (https://github.com/DimensionalDevelopment/LegacyLauncher)
        try {
            // Copy launchwrapper-2.0.jar library
            File launchWrapperTargetFile = new File(ProfileFinder.getGameDir(), "libraries/net/minecraft/launchwrapper/2.0/launchwrapper-2.0.jar");
            if (!launchWrapperTargetFile.exists()) {
                launchWrapperTargetFile.getParentFile().mkdirs();
                // jar_ is workaround for https://github.com/johnrengelman/shadow/issues/276
                try (InputStream is = VanillaFixLoadingPlugin.class.getResourceAsStream("/launchwrapper-2.0.jar_");
                     OutputStream os = new FileOutputStream(launchWrapperTargetFile)) {
                    IOUtils.copy(is, os);
                }
            }

            // Replace launchwrapper in profile json
            File profileFile = ProfileFinder.getCurrentForgeProfileFile();
            log.info("Replacing LaunchWrapper in: " + profileFile);
            String profileString = new String(Files.readAllBytes(profileFile.toPath()));
            // TODO: Deserialize profile json instead for more flexibility
            String newProfileString = StringUtils.replace(profileString,
                    "net.minecraft:launchwrapper:1.12",
                    "net.minecraft:launchwrapper:2.0"
            );
            if (!newProfileString.equals(profileString)) {
                Files.write(profileFile.toPath(), newProfileString.getBytes());

                // Restart minecraft and minecraft launcher with same arguments
                File libraryPath = new File(System.getProperty("java.library.path"));
                File newLibraryPath = Files.createTempDirectory("natives").toFile();
                FileUtils.copyDirectory(libraryPath, newLibraryPath);

                final String command = makeNewCommandLine(newLibraryPath.getAbsolutePath());

                log.info("Restarting Minecraft with command: " + command);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        Runtime.getRuntime().exec(command);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
                // TODO: kill vanilla launcher process too, it caches the json and undoes the changes
                Method shutdownMethod = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", int.class);
                shutdownMethod.setAccessible(true);
                shutdownMethod.invoke(null, 0);
            }
        } catch (Throwable t) {
            log.error("Failed replacing LaunchWrapper in profile json", t);
        }

        // Trust the "IdenTrust DST Root CA X3" certificate (used by Let's Encrypt, which is used by paste.dimdev.org)
        // TODO: Trust two other certificates, use same alias: https://bugs.openjdk.java.net/browse/JDK-8161008
        try (InputStream keyStoreInputStream = VanillaFixLoadingPlugin.class.getResourceAsStream("/dst_root_ca_x3.jks")) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, "password".toCharArray());
            SSLUtils.trustCertificates(keyStore);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException();
        }

        // Initialize StacktraceDeobfuscator
        File modDir = new File(Launch.minecraftHome, "config/vanillafix");
        modDir.mkdirs();

        // Initialize StacktraceDeobfuscator
        log.info("Initializing StacktraceDeobfuscator");
        try {
            File mappings = new File(modDir, "methods-" + MCP_VERSION + ".csv");
            if (mappings.exists()) {
                log.info("Found MCP method mappings: " + mappings.getName());
            } else {
                log.info("Downloading MCP method mappings to: " + mappings.getName());
            }
            StacktraceDeobfuscator.init(mappings, MCP_VERSION);
        } catch (Exception e) {
            log.error("Failed to get MCP data!", e);
        }
        log.info("Done initializing StacktraceDeobfuscator");

        // Install the log exception deobfuscation rewrite policy
        DeobfuscatingRewritePolicy.install();
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

    @Override public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override public String getModContainerClass() {
        return null;
    }

    @Nullable @Override public String getSetupClass() {
        return null;
    }

    @Override public void injectData(Map<String, Object> data) {}

    @Override public String getAccessTransformerClass() {
        return null;
    }
}
