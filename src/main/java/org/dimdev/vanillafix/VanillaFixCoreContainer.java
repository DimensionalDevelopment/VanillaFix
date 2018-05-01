package org.dimdev.vanillafix;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class VanillaFixCoreContainer extends DummyModContainer {

    public VanillaFixCoreContainer() {
        super(loadMetadataJson(VanillaFixCoreContainer.class, "vanillafix"));
    }

    private static ModMetadata loadMetadataJson(Class<?> class0, String modid) {
        try (InputStream in = class0.getResourceAsStream("/mcmod.info")) {
            return MetadataCollection.from(in, modid).getMetadataForId(modid, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    @Override
    public VersionRange acceptableMinecraftVersionRange() {
        return VersionParser.parseRange("(0.0.0,)");
    }
}
