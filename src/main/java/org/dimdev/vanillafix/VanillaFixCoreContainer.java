package org.dimdev.vanillafix;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;

import java.util.Collections;
import java.util.List;

public class VanillaFixCoreContainer extends DummyModContainer {

    public VanillaFixCoreContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "vanillafixcore";
        meta.name = "VanillaFix";
        meta.version = "${version}";
    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return Collections.emptyList();
    }
}
