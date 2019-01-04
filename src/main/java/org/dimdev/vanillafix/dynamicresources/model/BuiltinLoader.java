package org.dimdev.vanillafix.dynamicresources.model;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;

import java.io.FileNotFoundException;

public class BuiltinLoader implements ICustomModelLoader {
    public static final BuiltinLoader INSTANCE = new BuiltinLoader();

    private static final String EMPTY_MODEL_RAW = "{    'elements': [        {   'from': [0, 0, 0],            'to': [16, 16, 16],            'faces': {                'down': {'uv': [0, 0, 16, 16], 'texture': '' }            }        }    ]}".replaceAll("'", "\"");
    private static final String MISSING_MODEL_MESH = "{    'textures': {       'particle': 'missingno',       'missingno': 'missingno'    },    'elements': [         {  'from': [ 0, 0, 0 ],            'to': [ 16, 16, 16 ],            'faces': {                'down':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'down',  'texture': '#missingno' },                'up':    { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'up',    'texture': '#missingno' },                'north': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'north', 'texture': '#missingno' },                'south': { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'south', 'texture': '#missingno' },                'west':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'west',  'texture': '#missingno' },                'east':  { 'uv': [ 0, 0, 16, 16 ], 'cullface': 'east',  'texture': '#missingno' }            }        }    ]}".replaceAll("'", "\"");
    public static final ModelBlock MODEL_GENERATED = ModelBlock.deserialize(EMPTY_MODEL_RAW);
    public static final ModelBlock MODEL_ENTITY = ModelBlock.deserialize(EMPTY_MODEL_RAW);
    public static final ModelBlock MODEL_MISSING = ModelBlock.deserialize(MISSING_MODEL_MESH);
    public static final ModelResourceLocation MISSING_MODEL_LOCATION = new ModelResourceLocation("builtin/missing", "missing");
    public static final IModel WRAPPED_MODEL_MISSING = new VanillaModelWrapper(MISSING_MODEL_LOCATION, MODEL_MISSING);

    static {
        MODEL_GENERATED.name = "generation marker";
        MODEL_ENTITY.name = "block entity marker";
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getPath().startsWith("builtin/") ||
               modelLocation.getPath().startsWith("block/builtin/") ||
               modelLocation.getPath().startsWith("item/builtin/");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        String path = modelLocation.getPath();

        if ("builtin/generated".equals(path) || "block/builtin/generated".equals(path) || "item/builtin/generated".equals(path)) { // TODO: why is this necessary?
            return ItemLayerModel.INSTANCE; //new VanillaModelWrapper(modelLocation, MODEL_GENERATED);
        }

        if ("builtin/entity".equals(path)) {
            return new VanillaModelWrapper(modelLocation, MODEL_ENTITY);
        }

        if ("builtin/missing".equals(path)) {
            return WRAPPED_MODEL_MISSING;
        }

        throw new FileNotFoundException(modelLocation.toString());
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {}

    @Override
    public String toString() {
        return "BuiltinLoader";
    }
}
