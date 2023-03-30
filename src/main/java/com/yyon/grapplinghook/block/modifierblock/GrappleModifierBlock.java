package com.yyon.grapplinghook.block.modifierblock;

import com.yyon.grapplinghook.blockentity.GrappleModifierBlockEntity;
import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.item.GrapplehookItem;
import com.yyon.grapplinghook.item.upgrade.BaseUpgradeItem;
import com.yyon.grapplinghook.registry.GrappleModItems;
import com.yyon.grapplinghook.util.Check;
import com.yyon.grapplinghook.util.GrappleCustomization;
import com.yyon.grapplinghook.util.Vec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrappleModifierBlock extends BlockWithEntity {

	public GrappleModifierBlock() {
		super(Block.Settings.of(Material.STONE).strength(1.5f));
	}


	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new GrappleModifierBlockEntity(pos,state);
	}
	
	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder lootContext) {
		List<ItemStack> drops = new ArrayList<>();
		drops.add(new ItemStack(this.asItem()));

		BlockEntity ent = lootContext.getNullable(LootContextParameters.BLOCK_ENTITY);

		if (!(ent instanceof GrappleModifierBlockEntity tile)) return drops;

		for (GrappleCustomization.UpgradeCategories category : GrappleCustomization.UpgradeCategories.values()) {
			if (tile.unlockedCategories.containsKey(category) && tile.unlockedCategories.get(category)) {
				drops.add(new ItemStack(category.getItem()));
			}
		}
		return drops;
	}


    @Override
	@NotNull
	public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockHitResult raytraceresult) {
		ItemStack helditemstack = playerIn.getStackInHand(hand);
		Item helditem = helditemstack.getItem();

		if (helditem instanceof BaseUpgradeItem upgradeItem) {
			if (worldIn.isClient)
				return ActionResult.PASS;

			BlockEntity ent = worldIn.getBlockEntity(pos);
			GrappleModifierBlockEntity tile = (GrappleModifierBlockEntity) ent;

			if (Check.missingTileEntity(tile, playerIn, worldIn, pos))
				return ActionResult.FAIL;

			GrappleCustomization.UpgradeCategories category = upgradeItem.category;
			if (category == null)
				return ActionResult.FAIL;

			if (tile.isUnlocked(category)) {
				playerIn.sendMessage(Text.literal("Already has upgrade: " + category.getName()));

			} else {
				if (!playerIn.isCreative())
					playerIn.setStackInHand(hand, ItemStack.EMPTY);

				tile.unlockCategory(category);

				playerIn.sendMessage(Text.literal("Applied upgrade: " + category.getName()));
			}


		} else if (helditem instanceof GrapplehookItem) {
			if (worldIn.isClient)
				return ActionResult.PASS;

			BlockEntity ent = worldIn.getBlockEntity(pos);
			GrappleModifierBlockEntity tile = (GrappleModifierBlockEntity) ent;

			if (Check.missingTileEntity(tile, playerIn, worldIn, pos))
				return ActionResult.FAIL;

			GrappleCustomization custom = tile.customization;
			GrappleModItems.GRAPPLING_HOOK.get().setCustomOnServer(helditemstack, custom);

			playerIn.sendMessage(Text.literal("Applied configuration"));

		} else if (helditem == Items.DIAMOND_BOOTS) {
			if (worldIn.isClient) {
				playerIn.sendMessage(Text.literal("You are not permitted to make Long Fall Boots here.").formatted(Formatting.RED));
				return ActionResult.PASS;
			}

			if (!GrappleConfig.getConf().longfallboots.longfallbootsrecipe)
				return ActionResult.SUCCESS;

			boolean gaveitem = false;

			if (!helditemstack.hasEnchantments())
				return ActionResult.FAIL;

			Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(helditemstack);
			if (enchantments.getOrDefault(Enchantments.FEATHER_FALLING, -1) >= 4) {
				ItemStack newitemstack = new ItemStack(GrappleModItems.LONG_FALL_BOOTS.get());
				EnchantmentHelper.set(enchantments, newitemstack);
				playerIn.setStackInHand(hand, newitemstack);
				gaveitem = true;
			}


			if (!gaveitem) {
				playerIn.sendMessage(Text.literal("Right click with diamond boots enchanted with feather falling IV to get long fall boots"));
			}


		} else if (helditem == Items.DIAMOND) {
			this.easterEgg(worldIn, pos, playerIn);

		} else {
			if ((!worldIn.isClient) || hand != Hand.MAIN_HAND)
				return ActionResult.PASS;

			BlockEntity ent = worldIn.getBlockEntity(pos);
			GrappleModifierBlockEntity tile = (GrappleModifierBlockEntity) ent;

			GrappleModClient.get().openModifierScreen(tile);
		}

		return ActionResult.SUCCESS;
	}
    
    @Override
	@NotNull
    public BlockRenderType getRenderType(BlockState pState) {
        return BlockRenderType.MODEL;
    }

	public void easterEgg(World worldIn, BlockPos pos, PlayerEntity playerIn) {
		int spacing = 3;
		Vec[] positions = new Vec[] {new Vec(-spacing*2, 0, 0), new Vec(-spacing, 0, 0), new Vec(0, 0, 0), new Vec(spacing, 0, 0), new Vec(2*spacing, 0, 0)};
		int[] colors = new int[] {0x5bcffa, 0xf5abb9, 0xffffff, 0xf5abb9, 0x5bcffa};
		
		for (int i = 0; i < positions.length; i++) {
			Vec newpos = new Vec(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
			Vec toPlayer = Vec.positionVec(playerIn).sub(newpos);
			double angle = toPlayer.length() == 0 ? 0 : toPlayer.getYaw();
			newpos = newpos.add(positions[i].rotateYaw(Math.toRadians(angle)));
			
			NbtCompound explosion = new NbtCompound();
	        explosion.putByte("Type", (byte) FireworkRocketItem.Type.SMALL_BALL.getId());
	        explosion.putBoolean("Trail", true);
	        explosion.putBoolean("Flicker", false);
	        explosion.putIntArray("Colors", new int[] {colors[i]});
	        explosion.putIntArray("FadeColors", new int[] {});
	        NbtList list = new NbtList();
	        list.add(explosion);

	        NbtCompound fireworks = new NbtCompound();
	        fireworks.put("Explosions", list);

	        NbtCompound nbt = new NbtCompound();
	        nbt.put("Fireworks", fireworks);

	        ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
	        stack.setNbt(nbt);

			FireworkRocketEntity firework = new FireworkRocketEntity(worldIn, playerIn, newpos.x, newpos.y, newpos.z, stack);
			NbtCompound fireworkSave = new NbtCompound();
			firework.writeCustomDataToNbt(fireworkSave);
			fireworkSave.putInt("LifeTime", 15);
			firework.readCustomDataFromNbt(fireworkSave);
			worldIn.spawnEntity(firework);
		}
	}


}
