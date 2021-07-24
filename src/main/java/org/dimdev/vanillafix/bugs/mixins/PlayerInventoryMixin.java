package org.dimdev.vanillafix.bugs.mixins;

import org.dimdev.vanillafix.util.annotation.MixinConfigValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

@MixinConfigValue(category = "bugFixes", value = "fixRecipeBookIngredientsWithTags")
@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
	/**
	 * Compare items by item type rather than NBT when looking for items for the crafting
	 * recipe. Note that the item is still checked (in indexOf) to make sure it is not
	 * enchanted or renamed.
	 * <p>
	 * Bugs fixed:
	 * - https://bugs.mojang.com/browse/MC-129057
	 */
	//@Redirect(method = "indexOf", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;areItemsEqual(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
	//private boolean stackEqualExact(PlayerInventory inventoryPlayer, ItemStack left, ItemStack right) {
	//	return left.getItem() == right.getItem();
	//}
}
