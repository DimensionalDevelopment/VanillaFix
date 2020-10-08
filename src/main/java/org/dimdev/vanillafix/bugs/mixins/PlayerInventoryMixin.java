package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.util.config.ModConfigCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@ModConfigCondition(category = "bugFixes", key = "fixRecipeBookIngredientsWithTags")
@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    /**
     * Compare items by item type rather than NBT when looking for items for the crafting
     * recipe. Note that the item is still checked (in findSlotMatchingUnusedItem) to
     * make sure it is not enchanted or renamed.
     * <p>
     * Bugs fixed:
     * - https://bugs.mojang.com/browse/MC-129057
     */
    @Redirect(method = "method_7371", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;areItemsEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean stackEqualExact(PlayerInventory inventoryPlayer, ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem();
    }
}
