package com.yyon.grapplinghook.item;

import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.client.keybind.MCKeys;
import com.yyon.grapplinghook.controller.GrappleController;
import com.yyon.grapplinghook.util.GrappleModUtils;
import com.yyon.grapplinghook.util.Vec;
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
import java.util.List;

public class ForcefieldItem extends Item {
	public ForcefieldItem() {
		super(new Item.Settings().maxCount(1));
	}
	
	public void doRightClick(ItemStack stack, World worldIn, PlayerEntity player) {
		if (worldIn.isClient) {
			int playerid = player.getId();
			GrappleController oldController = GrappleModClient.get().unregisterController(playerid);
			if (oldController == null || oldController.controllerId == GrappleModUtils.AIR_FRICTION_ID) {
				GrappleModClient.get().createControl(GrappleModUtils.REPEL_ID, -1, playerid, worldIn, new Vec(0,0,0), null, null);
			}
		}
	}

    @Override
    public TypedActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
    	ItemStack stack = playerIn.getStackInHand(hand);
        this.doRightClick(stack, worldIn, playerIn);
        
    	return TypedActionResult.success(stack);
	}
    
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World world, List<Text> list, TooltipContext par4) {
		list.add(Text.translatable("grappletooltip.repelleritem.desc"));
		list.add(Text.translatable("grappletooltip.repelleritem2.desc"));
		list.add(Text.literal(""));
		list.add(Text.literal(GrappleModClient.get().getKeyname(MCKeys.keyBindUseItem) + Text.translatable("grappletooltip.repelleritemon.desc").getString()));
		list.add(Text.literal(GrappleModClient.get().getKeyname(MCKeys.keyBindUseItem) + Text.translatable("grappletooltip.repelleritemoff.desc").getString()));
		list.add(Text.literal(GrappleModClient.get().getKeyname(MCKeys.keyBindSneak) + Text.translatable("grappletooltip.repelleritemslow.desc").getString()));
		list.add(Text.literal(GrappleModClient.get().getKeyname(MCKeys.keyBindForward) + ", " +
				GrappleModClient.get().getKeyname(MCKeys.keyBindLeft) + ", " +
				GrappleModClient.get().getKeyname(MCKeys.keyBindBack) + ", " +
				GrappleModClient.get().getKeyname(MCKeys.keyBindRight) +
				" " + Text.translatable("grappletooltip.repelleritemmove.desc").getString()));
	}
}
