package org.dimdev.vanillafix.bugs.mixins;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Fix a memory leak caused by the world object still being pointed to by the static
 * ENCHANTMENT_ fields. Instead of modifying those static fields, make a new instance
 * of IModifier every use. Overwriting these methods is necessary, it would require a
 * redirect for every single line otherwise.
 * <p>
 * Bugs fixed:
 * - https://bugs.mojang.com/browse/MC-128547
 */
@Mixin(EnchantmentHelper.class)
public final class MixinEnchantmentHelper {
    @Shadow private static void applyEnchantmentModifierArray(EnchantmentHelper.IModifier modifier, Iterable<ItemStack> stacks) {}
    @Shadow private static void applyEnchantmentModifier(EnchantmentHelper.IModifier modifier, ItemStack stack) {}

    /** @reason Fix memory leak. See class comment. */
    @Overwrite
    public static int getEnchantmentModifierDamage(Iterable<ItemStack> stacks, DamageSource source) {
        EnchantmentHelper.ModifierDamage enchantmentModifierDamage = new EnchantmentHelper.ModifierDamage();
        enchantmentModifierDamage.damageModifier = 0;
        enchantmentModifierDamage.source = source;
        applyEnchantmentModifierArray(enchantmentModifierDamage, stacks);
        return enchantmentModifierDamage.damageModifier;
    }

    /** @reason Fix memory leak. See mixin class comment. */
    @Overwrite
    public static float getModifierForCreature(ItemStack stack, EnumCreatureAttribute creatureAttribute) {
        EnchantmentHelper.ModifierLiving enchantmentModifierLiving = new EnchantmentHelper.ModifierLiving();
        enchantmentModifierLiving.livingModifier = 0.0F;
        enchantmentModifierLiving.entityLiving = creatureAttribute;
        applyEnchantmentModifier(enchantmentModifierLiving, stack);
        return enchantmentModifierLiving.livingModifier;
    }

    /** @reason Fix memory leak. See mixin class comment. */
    @Overwrite
    public static void applyThornEnchantments(EntityLivingBase user, Entity attacker) {
        EnchantmentHelper.HurtIterator enchantmentIteratorHurt = new EnchantmentHelper.HurtIterator();
        enchantmentIteratorHurt.attacker = attacker;
        enchantmentIteratorHurt.user = user;

        if (user != null) {
            applyEnchantmentModifierArray(enchantmentIteratorHurt, user.getEquipmentAndArmor());
        }

        if (attacker instanceof EntityPlayer) {
            applyEnchantmentModifier(enchantmentIteratorHurt, user.getHeldItemMainhand());
        }
    }

    /** @reason Fix memory leak. See mixin class comment. */
    @Overwrite
    public static void applyArthropodEnchantments(EntityLivingBase user, Entity target) {
        EnchantmentHelper.DamageIterator enchantmentIteratorDamage = new EnchantmentHelper.DamageIterator();
        enchantmentIteratorDamage.user = user;
        enchantmentIteratorDamage.target = target;

        if (user != null) {
            applyEnchantmentModifierArray(enchantmentIteratorDamage, user.getEquipmentAndArmor());
        }

        if (user instanceof EntityPlayer) {
            applyEnchantmentModifier(enchantmentIteratorDamage, user.getHeldItemMainhand());
        }
    }
}
