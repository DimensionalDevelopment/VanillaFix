package org.dimdev.vanillafix;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.SSLUtils;
import org.dimdev.vanillafix.crashes.DeobfuscatingRewritePolicy;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import sun.misc.URLClassPath;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
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

    static {
        log.info("Initializing VanillaFix");
        config = new LoadingConfig(new File(Launch.minecraftHome, "config/vanillafix.cfg"));

        trustIdenTrust();
        initStacktraceDeobfuscator();
        initMixin();
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

    private static void initMixin() {
        MixinBootstrap.init();

        if (config.bugFixes) {
            log.info("Initializing Bug Fix Mixins");
            Mixins.addConfiguration("mixins.vanillafix.bugs.json");
        }
        if (config.crashFixes) {
            log.info("Initializing Crash Fix Mixins");
            Mixins.addConfiguration("mixins.vanillafix.crashes.json");
        }
        if (config.profiler) {
            log.info("Initializing Profiler Improvement Mixins");
            Mixins.addConfiguration("mixins.vanillafix.profiler.json");
        }
        if (config.textureFixes) {
            log.info("Initializing Texture Fix Mixins");
            Mixins.addConfiguration("mixins.vanillafix.textures.json");
        }
        if (config.modSupport) {
            log.info("Initializing Mod Support Mixins");
            Mixins.addConfiguration("mixins.vanillafix.modsupport.json");
        }
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
