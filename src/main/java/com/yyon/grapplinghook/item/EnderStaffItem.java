package com.yyon.grapplinghook.item;

import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.client.keybind.MCKeys;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

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

public class EnderStaffItem extends Item {
	
	public EnderStaffItem() {
		super(new Item.Settings().maxCount(1));
	}
	
	public void doRightClick(World worldIn, PlayerEntity player) {
		if (!worldIn.isClient) return;
		GrappleModClient.get().launchPlayer(player);
	}
	
    @Override
	@NotNull
    public TypedActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
    	ItemStack stack = playerIn.getStackInHand(hand);
        this.doRightClick(worldIn, playerIn);

    	return TypedActionResult.success(stack);
	}
    
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World world, List<Text> list, TooltipContext par4) {
		list.add(Text.translatable("grappletooltip.launcheritem.desc"));
		list.add(Text.literal(""));
		list.add(Text.translatable("grappletooltip.launcheritemaim.desc"));
		list.add(Text.literal(GrappleModClient.get().getKeyname(MCKeys.keyBindUseItem) + Text.translatable("grappletooltip.launcheritemcontrols.desc").getString()));
	}
}
