package org.dimdev.vanillafix.dynamicresources.model;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IRegistryDelegate;

import java.lang.reflect.Field;
import java.util.*;

public class ModelLocationInformation {
    private static Map<Item, List<String>> variantNames = new HashMap<>();
    private static HashMap<ModelResourceLocation, ResourceLocation> inventoryVariantLocations = new HashMap<>();
    private static HashMap<ResourceLocation, Block> blockstateLocationToBlock = new HashMap<>();

    public static void init(BlockStateMapper blockStateMapper) {
        // Make variant names map
        Map<IRegistryDelegate<Item>, Set<String>> customVariantNames;
        try {
            Field customVariantNamesField = ModelBakery.class.getDeclaredField("customVariantNames");
            customVariantNamesField.setAccessible(true);
            // noinspection unchecked
            customVariantNames = (Map<net.minecraftforge.registries.IRegistryDelegate<Item>, Set<String>>) customVariantNamesField.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        // <editor-fold defaultstate="collapsed" desc="Vanilla variant names (from ModelBakery)">
        variantNames.put(Item.getItemFromBlock(Blocks.STONE), Lists.newArrayList("stone", "granite", "granite_smooth", "diorite", "diorite_smooth", "andesite", "andesite_smooth"));
        variantNames.put(Item.getItemFromBlock(Blocks.DIRT), Lists.newArrayList("dirt", "coarse_dirt", "podzol"));
        variantNames.put(Item.getItemFromBlock(Blocks.PLANKS), Lists.newArrayList("oak_planks", "spruce_planks", "birch_planks", "jungle_planks", "acacia_planks", "dark_oak_planks"));
        variantNames.put(Item.getItemFromBlock(Blocks.SAPLING), Lists.newArrayList("oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling", "acacia_sapling", "dark_oak_sapling"));
        variantNames.put(Item.getItemFromBlock(Blocks.SAND), Lists.newArrayList("sand", "red_sand"));
        variantNames.put(Item.getItemFromBlock(Blocks.LOG), Lists.newArrayList("oak_log", "spruce_log", "birch_log", "jungle_log"));
        variantNames.put(Item.getItemFromBlock(Blocks.LEAVES), Lists.newArrayList("oak_leaves", "spruce_leaves", "birch_leaves", "jungle_leaves"));
        variantNames.put(Item.getItemFromBlock(Blocks.SPONGE), Lists.newArrayList("sponge", "sponge_wet"));
        variantNames.put(Item.getItemFromBlock(Blocks.SANDSTONE), Lists.newArrayList("sandstone", "chiseled_sandstone", "smooth_sandstone"));
        variantNames.put(Item.getItemFromBlock(Blocks.RED_SANDSTONE), Lists.newArrayList("red_sandstone", "chiseled_red_sandstone", "smooth_red_sandstone"));
        variantNames.put(Item.getItemFromBlock(Blocks.TALLGRASS), Lists.newArrayList("dead_bush", "tall_grass", "fern"));
        variantNames.put(Item.getItemFromBlock(Blocks.DEADBUSH), Lists.newArrayList("dead_bush"));
        variantNames.put(Item.getItemFromBlock(Blocks.WOOL), Lists.newArrayList("black_wool", "red_wool", "green_wool", "brown_wool", "blue_wool", "purple_wool", "cyan_wool", "silver_wool", "gray_wool", "pink_wool", "lime_wool", "yellow_wool", "light_blue_wool", "magenta_wool", "orange_wool", "white_wool"));
        variantNames.put(Item.getItemFromBlock(Blocks.YELLOW_FLOWER), Lists.newArrayList("dandelion"));
        variantNames.put(Item.getItemFromBlock(Blocks.RED_FLOWER), Lists.newArrayList("poppy", "blue_orchid", "allium", "houstonia", "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"));
        variantNames.put(Item.getItemFromBlock(Blocks.STONE_SLAB), Lists.newArrayList("stone_slab", "sandstone_slab", "cobblestone_slab", "brick_slab", "stone_brick_slab", "nether_brick_slab", "quartz_slab"));
        variantNames.put(Item.getItemFromBlock(Blocks.STONE_SLAB2), Lists.newArrayList("red_sandstone_slab"));
        variantNames.put(Item.getItemFromBlock(Blocks.STAINED_GLASS), Lists.newArrayList("black_stained_glass", "red_stained_glass", "green_stained_glass", "brown_stained_glass", "blue_stained_glass", "purple_stained_glass", "cyan_stained_glass", "silver_stained_glass", "gray_stained_glass", "pink_stained_glass", "lime_stained_glass", "yellow_stained_glass", "light_blue_stained_glass", "magenta_stained_glass", "orange_stained_glass", "white_stained_glass"));
        variantNames.put(Item.getItemFromBlock(Blocks.MONSTER_EGG), Lists.newArrayList("stone_monster_egg", "cobblestone_monster_egg", "stone_brick_monster_egg", "mossy_brick_monster_egg", "cracked_brick_monster_egg", "chiseled_brick_monster_egg"));
        variantNames.put(Item.getItemFromBlock(Blocks.STONEBRICK), Lists.newArrayList("stonebrick", "mossy_stonebrick", "cracked_stonebrick", "chiseled_stonebrick"));
        variantNames.put(Item.getItemFromBlock(Blocks.WOODEN_SLAB), Lists.newArrayList("oak_slab", "spruce_slab", "birch_slab", "jungle_slab", "acacia_slab", "dark_oak_slab"));
        variantNames.put(Item.getItemFromBlock(Blocks.COBBLESTONE_WALL), Lists.newArrayList("cobblestone_wall", "mossy_cobblestone_wall"));
        variantNames.put(Item.getItemFromBlock(Blocks.ANVIL), Lists.newArrayList("anvil_intact", "anvil_slightly_damaged", "anvil_very_damaged"));
        variantNames.put(Item.getItemFromBlock(Blocks.QUARTZ_BLOCK), Lists.newArrayList("quartz_block", "chiseled_quartz_block", "quartz_column"));
        variantNames.put(Item.getItemFromBlock(Blocks.STAINED_HARDENED_CLAY), Lists.newArrayList("black_stained_hardened_clay", "red_stained_hardened_clay", "green_stained_hardened_clay", "brown_stained_hardened_clay", "blue_stained_hardened_clay", "purple_stained_hardened_clay", "cyan_stained_hardened_clay", "silver_stained_hardened_clay", "gray_stained_hardened_clay", "pink_stained_hardened_clay", "lime_stained_hardened_clay", "yellow_stained_hardened_clay", "light_blue_stained_hardened_clay", "magenta_stained_hardened_clay", "orange_stained_hardened_clay", "white_stained_hardened_clay"));
        variantNames.put(Item.getItemFromBlock(Blocks.STAINED_GLASS_PANE), Lists.newArrayList("black_stained_glass_pane", "red_stained_glass_pane", "green_stained_glass_pane", "brown_stained_glass_pane", "blue_stained_glass_pane", "purple_stained_glass_pane", "cyan_stained_glass_pane", "silver_stained_glass_pane", "gray_stained_glass_pane", "pink_stained_glass_pane", "lime_stained_glass_pane", "yellow_stained_glass_pane", "light_blue_stained_glass_pane", "magenta_stained_glass_pane", "orange_stained_glass_pane", "white_stained_glass_pane"));
        variantNames.put(Item.getItemFromBlock(Blocks.LEAVES2), Lists.newArrayList("acacia_leaves", "dark_oak_leaves"));
        variantNames.put(Item.getItemFromBlock(Blocks.LOG2), Lists.newArrayList("acacia_log", "dark_oak_log"));
        variantNames.put(Item.getItemFromBlock(Blocks.PRISMARINE), Lists.newArrayList("prismarine", "prismarine_bricks", "dark_prismarine"));
        variantNames.put(Item.getItemFromBlock(Blocks.CARPET), Lists.newArrayList("black_carpet", "red_carpet", "green_carpet", "brown_carpet", "blue_carpet", "purple_carpet", "cyan_carpet", "silver_carpet", "gray_carpet", "pink_carpet", "lime_carpet", "yellow_carpet", "light_blue_carpet", "magenta_carpet", "orange_carpet", "white_carpet"));
        variantNames.put(Item.getItemFromBlock(Blocks.DOUBLE_PLANT), Lists.newArrayList("sunflower", "syringa", "double_grass", "double_fern", "double_rose", "paeonia"));
        variantNames.put(Items.COAL, Lists.newArrayList("coal", "charcoal"));
        variantNames.put(Items.FISH, Lists.newArrayList("cod", "salmon", "clownfish", "pufferfish"));
        variantNames.put(Items.COOKED_FISH, Lists.newArrayList("cooked_cod", "cooked_salmon"));
        variantNames.put(Items.DYE, Lists.newArrayList("dye_black", "dye_red", "dye_green", "dye_brown", "dye_blue", "dye_purple", "dye_cyan", "dye_silver", "dye_gray", "dye_pink", "dye_lime", "dye_yellow", "dye_light_blue", "dye_magenta", "dye_orange", "dye_white"));
        variantNames.put(Items.POTIONITEM, Lists.newArrayList("bottle_drinkable"));
        variantNames.put(Items.SKULL, Lists.newArrayList("skull_skeleton", "skull_wither", "skull_zombie", "skull_char", "skull_creeper", "skull_dragon"));
        variantNames.put(Items.SPLASH_POTION, Lists.newArrayList("bottle_splash"));
        variantNames.put(Items.LINGERING_POTION, Lists.newArrayList("bottle_lingering"));
        variantNames.put(Item.getItemFromBlock(Blocks.CONCRETE), Lists.newArrayList("black_concrete", "red_concrete", "green_concrete", "brown_concrete", "blue_concrete", "purple_concrete", "cyan_concrete", "silver_concrete", "gray_concrete", "pink_concrete", "lime_concrete", "yellow_concrete", "light_blue_concrete", "magenta_concrete", "orange_concrete", "white_concrete"));
        variantNames.put(Item.getItemFromBlock(Blocks.CONCRETE_POWDER), Lists.newArrayList("black_concrete_powder", "red_concrete_powder", "green_concrete_powder", "brown_concrete_powder", "blue_concrete_powder", "purple_concrete_powder", "cyan_concrete_powder", "silver_concrete_powder", "gray_concrete_powder", "pink_concrete_powder", "lime_concrete_powder", "yellow_concrete_powder", "light_blue_concrete_powder", "magenta_concrete_powder", "orange_concrete_powder", "white_concrete_powder"));
        variantNames.put(Item.getItemFromBlock(Blocks.AIR), Collections.emptyList());
        variantNames.put(Item.getItemFromBlock(Blocks.OAK_FENCE_GATE), Lists.newArrayList("oak_fence_gate"));
        variantNames.put(Item.getItemFromBlock(Blocks.OAK_FENCE), Lists.newArrayList("oak_fence"));
        variantNames.put(Items.OAK_DOOR, Lists.newArrayList("oak_door"));
        variantNames.put(Items.BOAT, Lists.newArrayList("oak_boat"));
        variantNames.put(Items.TOTEM_OF_UNDYING, Lists.newArrayList("totem"));
        // </editor-fold>
        for (Map.Entry<IRegistryDelegate<Item>, Set<String>> e : customVariantNames.entrySet()) {
            variantNames.put(e.getKey().get(), Lists.newArrayList(e.getValue().iterator()));
        }

        // Make inventory variant -> location map
        for (Item item : Item.REGISTRY) {
            for (String s : getVariantNames(item)) {
                ResourceLocation itemLocation = getItemLocation(s);
                ModelResourceLocation inventoryVariant = getInventoryVariant(s);
                inventoryVariantLocations.put(inventoryVariant, itemLocation);
            }
        }

        // Make blockstate -> block map
        for (Block block : Block.REGISTRY) {
            for (ResourceLocation location : blockStateMapper.getBlockstateLocations(block)) {
                blockstateLocationToBlock.put(location, block);
            }
        }
    }

    public static ResourceLocation getInventoryVariantLocation(ModelResourceLocation inventoryVariant) {
        return inventoryVariantLocations.get(inventoryVariant);
    }

    public static void addInventoryVariantLocation(ModelResourceLocation inventoryVariant, ResourceLocation location) {
        inventoryVariantLocations.put(inventoryVariant, location);
    }

    public static ResourceLocation getItemLocation(String location) {
        ResourceLocation resourcelocation = new ResourceLocation(location.replaceAll("#.*", ""));
        return new ResourceLocation(resourcelocation.getNamespace(), "item/" + resourcelocation.getPath());
    }

    public static ModelResourceLocation getInventoryVariant(String variant) {
        if (variant.contains("#")) {
            return new ModelResourceLocation(variant);
        }
        return new ModelResourceLocation(variant, "inventory");
    }

    public static List<String> getVariantNames(Item item) {
        List<String> list = variantNames.get(item);

        if (list == null) {
            list = Collections.singletonList(Item.REGISTRY.getNameForObject(item).toString());
        }

        return list;
    }

    public static Block getBlockFromBlockstateLocation(ResourceLocation blockstateLocation) {
        return blockstateLocationToBlock.get(blockstateLocation);
    }
}
