package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {
    /**
     * Compare items by item type and meta rather than NBT when looking for items for the
     * crafting recipe. Note that the item is still checked (in findSlotMatchingUnusedItem)
     * to make sure it is not enchanted or renamed. If the recipe item has meta 32767, any
     * item meta is accepted (see Ingredient.apply).
     * <p>
     * Bugs fixed:
     * - https://bugs.mojang.com/browse/MC-129057
     */
    @Redirect(method = "findSlotMatchingUnusedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/InventoryPlayer;stackEqualExact(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean stackEqualExact(InventoryPlayer inventoryPlayer, ItemStack stack1, ItemStack stack2) {
        return stack1.getItem() == stack2.getItem() && (stack1.getMetadata() == 32767 || stack1.getMetadata() == stack2.getMetadata());
    }
}
