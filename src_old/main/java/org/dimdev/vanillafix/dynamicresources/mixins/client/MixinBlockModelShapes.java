package org.dimdev.vanillafix.dynamicresources.mixins.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(BlockModelShapes.class)
public abstract class MixinBlockModelShapes {
    @Shadow @Final private ModelManager modelManager;
    @Shadow @Final private BlockStateMapper blockStateMapper;
    @Shadow public abstract ModelManager getModelManager();

    private Map<IBlockState, ModelResourceLocation> modelLocations;

    /**
     * @reason Don't get all models during init (with dynamic loading, that would
     * generate them all). Just store location instead.
     **/
    @Overwrite
    public void reloadModels() {
        modelLocations = blockStateMapper.putAllStateModelLocations();
    }

    /**
     * @reason Get the stored location for that state, and get the model from
     * that location from the model manager.
     **/
    @Overwrite
    public IBakedModel getModelForState(IBlockState state) {
        IBakedModel model = modelManager.getModel(modelLocations.get(state));
        if (model == null) {
            model = modelManager.getMissingModel();
        }
        return model;
    }
}
