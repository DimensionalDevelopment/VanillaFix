package org.dimdev.vanillafix.idlimit.mixins;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Rewrite most of the class to support an unlimited number of IDs (map rather than array). **/
@Mixin(StatList.class)
public final class MixinStatList {
    @Shadow @Final protected static Map<String, StatBase> ID_TO_STAT_MAP;
    @Shadow @Final public static List<StatBase> BASIC_STATS;
    @Shadow @Final public static List<StatCrafting> USE_ITEM_STATS;
    @Shadow @Final public static List<StatCrafting> MINE_BLOCK_STATS;

    @Shadow @Final public static List<StatBase> ALL_STATS;
    private static final Map<Block, StatBase> VF_BLOCK_STATS = new HashMap<>();
    private static final Map<Item, StatBase> VF_CRAFTS_STATS = new HashMap<>();
    private static final Map<Item, StatBase> VF_OBJECT_USE_STATS = new HashMap<>();
    private static final Map<Item, StatBase> VF_OBJECT_BREAK_STATS = new HashMap<>();
    private static final Map<Item, StatBase> VF_OBJECTS_PICKED_UP_STATS = new HashMap<>();
    private static final Map<Item, StatBase> VF_OBJECTS_DROPPED_STATS = new HashMap<>();

    @Overwrite @Nullable public static StatBase getBlockStats(Block block) { return VF_BLOCK_STATS.get(block); }
    @Overwrite @Nullable public static StatBase getCraftStats(Item item) { return VF_CRAFTS_STATS.get(item); }
    @Overwrite @Nullable public static StatBase getObjectUseStats(Item item) { return VF_OBJECT_USE_STATS.get(item); }
    @Overwrite @Nullable public static StatBase getObjectBreakStats(Item item) { return VF_OBJECT_BREAK_STATS.get(item); }
    @Overwrite @Nullable public static StatBase getObjectsPickedUpStats(Item item) { return VF_OBJECTS_PICKED_UP_STATS.get(item); }
    @Overwrite @Nullable public static StatBase getDroppedObjectStats(Item item) { return VF_OBJECTS_DROPPED_STATS.get(item); }

    @Overwrite
    public static void init() {
        initMiningStats();
        initStats();
        initItemDepleteStats();
        initCraftableStats();
        initPickedUpAndDroppedStats();
    }

    @Overwrite
    private static void initMiningStats() {
        for (Block block : Block.REGISTRY) {
            Item item = Item.getItemFromBlock(block);

            if (block.getEnableStats() && item != Items.AIR && getItemName(item) != null) {
                StatCrafting stat = new StatCrafting(
                        "stat.mineBlock.",
                        getItemName(item), new TextComponentTranslation("stat.mineBlock", new ItemStack(block).getTextComponent()),
                        item);

                MINE_BLOCK_STATS.add(stat);
                VF_BLOCK_STATS.put(block, stat);
                stat.registerStat();
            }
        }
    }

    @Overwrite
    private static void initStats() {
        for (Item item : Item.REGISTRY) {
            if (item != null && getItemName(item) != null) {
                StatCrafting stat = new StatCrafting("stat.useItem.",
                        getItemName(item),
                        new TextComponentTranslation("stat.useItem", new ItemStack(item).getTextComponent()),
                        item);

                VF_OBJECT_USE_STATS.put(item, stat);
                if (!(item instanceof ItemBlock)) USE_ITEM_STATS.add(stat);
                stat.registerStat();
            }
        }
    }

    @Overwrite
    private static void initCraftableStats() {
        Set<Item> craftableItems = Sets.newHashSet();

        for (IRecipe recipe : CraftingManager.REGISTRY) {
            ItemStack output = recipe.getRecipeOutput();
            if (!output.isEmpty()) craftableItems.add(recipe.getRecipeOutput().getItem());
        }

        for (ItemStack furnaceRecipeOutputs : FurnaceRecipes.instance().getSmeltingList().values()) {
            craftableItems.add(furnaceRecipeOutputs.getItem());
        }

        for (Item item : craftableItems) {
            if (item != null && getItemName(item) != null) {
                StatCrafting stat = new StatCrafting(
                        "stat.craftItem.",
                        getItemName(item),
                        new TextComponentTranslation("stat.craftItem", new ItemStack(item).getTextComponent()),
                        item);

                VF_CRAFTS_STATS.put(item, stat);
                stat.registerStat();
            }
        }
    }

    @Overwrite
    private static void initItemDepleteStats() {
        for (Item item : Item.REGISTRY) {
            if (item != null && getItemName(item) != null && item.isDamageable()) {
                StatCrafting stat = (new StatCrafting(
                        "stat.breakItem.",
                        getItemName(item),
                        new TextComponentTranslation("stat.breakItem", new ItemStack(item).getTextComponent()),
                        item));

                VF_OBJECT_BREAK_STATS.put(item, stat);
                stat.registerStat();
            }
        }
    }

    private static void initPickedUpAndDroppedStats() {
        for (Item item : Item.REGISTRY)
            if (item != null && getItemName(item) != null) {
                StatCrafting pickupStat = new StatCrafting(
                        "stat.pickup.",
                        getItemName(item),
                        new TextComponentTranslation("stat.pickup", new ItemStack(item).getTextComponent()),
                        item);

                StatCrafting dropStat = new StatCrafting(
                        "stat.drop.",
                        getItemName(item),
                        new TextComponentTranslation("stat.drop", new ItemStack(item).getTextComponent()),
                        item);

                VF_OBJECTS_PICKED_UP_STATS.put(item, pickupStat);
                VF_OBJECTS_DROPPED_STATS.put(item, dropStat);
                pickupStat.registerStat();
                dropStat.registerStat();
            }
    }

    private static String getItemName(Item itemIn) {
        ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(itemIn);
        return resourcelocation != null ? resourcelocation.toString().replace(':', '.') : null;
    }

    @Overwrite
    @Deprecated
    public static void reinit() { // Forge
        ID_TO_STAT_MAP.clear();
        BASIC_STATS.clear();
        USE_ITEM_STATS.clear();
        MINE_BLOCK_STATS.clear();

        // TODO: Optimize this, it hangs on server shutdown for a few seconds with
        ALL_STATS.removeAll(VF_BLOCK_STATS.values());
        ALL_STATS.removeAll(VF_CRAFTS_STATS.values());
        ALL_STATS.removeAll(VF_OBJECT_USE_STATS.values());
        ALL_STATS.removeAll(VF_OBJECT_BREAK_STATS.values());
        ALL_STATS.removeAll(VF_OBJECTS_PICKED_UP_STATS.values());
        ALL_STATS.removeAll(VF_OBJECTS_DROPPED_STATS.values());

        VF_BLOCK_STATS.clear();
        VF_CRAFTS_STATS.clear();
        VF_OBJECT_USE_STATS.clear();
        VF_OBJECT_BREAK_STATS.clear();
        VF_OBJECTS_PICKED_UP_STATS.clear();
        VF_OBJECTS_DROPPED_STATS.clear();

        List<StatBase> stats = Lists.newArrayList(ALL_STATS);
        ALL_STATS.clear();
        for (StatBase stat : stats) stat.registerStat();

        init();
    }
}
