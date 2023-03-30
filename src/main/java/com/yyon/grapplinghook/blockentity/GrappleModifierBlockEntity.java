package com.yyon.grapplinghook.blockentity;

import com.yyon.grapplinghook.network.NetworkManager;
import com.yyon.grapplinghook.network.serverbound.GrappleModifierMessage;
import com.yyon.grapplinghook.registry.GrappleModBlockEntities;
import com.yyon.grapplinghook.util.GrappleCustomization;
import com.yyon.grapplinghook.util.GrappleCustomization.UpgradeCategories;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class GrappleModifierBlockEntity extends BlockEntity {
	public HashMap<UpgradeCategories, Boolean> unlockedCategories = new HashMap<>();
	public GrappleCustomization customization;

	public GrappleModifierBlockEntity(BlockPos pos, BlockState state) {
		super(GrappleModBlockEntities.GRAPPLE_MODIFIER.get(), pos, state);
		this.customization = new GrappleCustomization();
	}

	private void triggerUpdate() {
		if(this.world != null) {
			BlockState state = this.world.getBlockState(pos);
			this.world.updateListeners(pos, state, state, 3);
			this.markDirty();
		}
	}

	public void unlockCategory(UpgradeCategories category) {
		unlockedCategories.put(category, true);
		this.triggerUpdate();
	}

	public void setCustomizationClient(GrappleCustomization customization) {
		this.customization = customization;
		NetworkManager.packetToServer(new GrappleModifierMessage(this.pos, this.customization));
		this.triggerUpdate();
	}

	public void setCustomizationServer(GrappleCustomization customization) {
		this.customization = customization;
		this.triggerUpdate();
	}

	public boolean isUnlocked(UpgradeCategories category) {
		return this.unlockedCategories.containsKey(category) && this.unlockedCategories.get(category);
	}

	@Override
	public void writeNbt(NbtCompound nbtTagCompound) {
		super.writeNbt(nbtTagCompound);

		NbtCompound unlockedNBT = nbtTagCompound.getCompound("unlocked");

		for (UpgradeCategories category : UpgradeCategories.values()) {
			String num = String.valueOf(category.toInt());
			boolean unlocked = this.isUnlocked(category);

			unlockedNBT.putBoolean(num, unlocked);
		}

		nbtTagCompound.put("unlocked", unlockedNBT);
		nbtTagCompound.put("customization", this.customization.writeNBT());
	}

	@Override
	public void readNbt(NbtCompound parentNBTTagCompound) {
		super.readNbt(parentNBTTagCompound); // The super call is required to load the tiles location

		NbtCompound unlockedNBT = parentNBTTagCompound.getCompound("unlocked");

		for (UpgradeCategories category : UpgradeCategories.values()) {
			String num = String.valueOf(category.toInt());
			boolean unlocked = unlockedNBT.getBoolean(num);

			this.unlockedCategories.put(category, unlocked);
		}

		NbtCompound custom = parentNBTTagCompound.getCompound("customization");
		this.customization.loadNBT(custom);
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		NbtCompound nbtTagCompound = new NbtCompound();
		this.writeNbt(nbtTagCompound);
		return BlockEntityUpdateS2CPacket.create(this);
	}


	/* Creates a tag containing all of the TileEntity information, used by vanilla to transmit from server to client */
	@Override
	@NotNull
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound nbtTagCompound = new NbtCompound();
		this.writeNbt(nbtTagCompound);
		return nbtTagCompound;
	}

}
