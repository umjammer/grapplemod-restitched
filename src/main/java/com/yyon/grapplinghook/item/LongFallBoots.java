package com.yyon.grapplinghook.item;

import com.yyon.grapplinghook.config.GrappleConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import java.util.List;

/*
 * This file is part of GrappleMod.

    GrappleMod is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GrappleMod is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GrappleMod.  If not, see <http://www.gnu.org/licenses/>.
 */

public class LongFallBoots extends ArmorItem {

	public LongFallBoots(ArmorMaterials material) {
	    super(material, Type.BOOTS, new Item.Settings().maxCount(1));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World world, List<Text> list, TooltipContext par4) {
		if (!stack.hasEnchantments()) {
			if (GrappleConfig.getConf().longfallboots.longfallbootsrecipe) {
				list.add(Text.translatable("grappletooltip.longfallbootsrecipe.desc"));
			}
		}
		list.add(Text.translatable("grappletooltip.longfallboots.desc"));
	}
}
