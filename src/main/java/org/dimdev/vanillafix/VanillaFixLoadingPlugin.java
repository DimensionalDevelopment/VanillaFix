package org.dimdev.vanillafix;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.dimdev.utils.SSLUtils;
import org.dimdev.vanillafix.crashes.DeobfuscatingRewritePolicy;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(-5000)
@IFMLLoadingPlugin.TransformerExclusions("org.dimdev.vanillafix.")
public class VanillaFixLoadingPlugin implements IFMLLoadingPlugin {

    public VanillaFixLoadingPlugin() {
        // Trust the "IdenTrust DST Root CA X3" certificate (used by Let's Encrypt, which is used by paste.dimdev.org)
        // TODO: Trust two other certificates, use same alias: https://bugs.openjdk.java.net/browse/JDK-8161008
        try (InputStream keyStoreInputStream = VanillaFixLoadingPlugin.class.getResourceAsStream("/dst_root_ca_x3.jks")) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreInputStream, "password".toCharArray());
            SSLUtils.trustCertificates(keyStore);
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new RuntimeException();
        }

        // Install the log exception deobfuscation rewrite policy
        DeobfuscatingRewritePolicy.install();

        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.vanillafix.bugs.json");
        Mixins.addConfiguration("mixins.vanillafix.crashes.json");
        Mixins.addConfiguration("mixins.vanillafix.profiler.json");
        Mixins.addConfiguration("mixins.vanillafix.textures.json");
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
