package org.dimdev.vanillafix;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.utils.SSLUtils;
import org.dimdev.vanillafix.crashes.DeobfuscatingRewritePolicy;
import org.dimdev.vanillafix.crashes.StacktraceDeobfuscator;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(-10000)
@IFMLLoadingPlugin.TransformerExclusions("org.dimdev.vanillafix.")
public class VanillaFixLoadingPlugin implements IFMLLoadingPlugin {
    private static final Logger log = LogManager.getLogger();
    private static final String MCP_VERSION = "20180601-1.12"; // TODO: Use version for current Minecraft version!
    private static boolean initialized = false;

    public VanillaFixLoadingPlugin() {
        initialize();

        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.vanillafix.bugs.json");
        Mixins.addConfiguration("mixins.vanillafix.crashes.json");
        Mixins.addConfiguration("mixins.vanillafix.profiler.json");
        Mixins.addConfiguration("mixins.vanillafix.textures.json");
        Mixins.addConfiguration("mixins.vanillafix.idlimit.json");
    }

    public static void initialize() {
        if (initialized) return;
        initialized = true;

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
