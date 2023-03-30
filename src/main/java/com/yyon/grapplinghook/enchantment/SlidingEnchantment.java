package com.yyon.grapplinghook.enchantment;

import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.config.GrappleConfigUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;

public class SlidingEnchantment extends Enchantment {
	public SlidingEnchantment() {
		super(GrappleConfigUtils.getRarityFromInt(GrappleConfig.getConf().enchantments.slide.enchant_rarity_sliding), EnchantmentTarget.ARMOR_FEET, new EquipmentSlot[] {EquipmentSlot.FEET});
	}
	
	@Override
    public int getMinPower(int enchantmentLevel)
    {
        return 1;
    }

	@Override
    public int getMaxPower(int enchantmentLevel)
    {
        return this.getMinPower(enchantmentLevel) + 40;
    }

	@Override
	public int getMaxLevel()
    {
        return 1;
    }
}
