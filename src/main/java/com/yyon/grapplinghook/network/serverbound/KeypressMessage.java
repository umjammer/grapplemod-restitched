package com.yyon.grapplinghook.network.serverbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.item.KeypressItem;
import com.yyon.grapplinghook.network.NetworkContext;
import com.yyon.grapplinghook.network.serverbound.BaseMessageServer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

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

public class KeypressMessage extends BaseMessageServer {
	
	KeypressItem.Keys key;
	boolean isDown;

    public KeypressMessage(PacketByteBuf buf) {
    	super(buf);
    }

    public KeypressMessage(KeypressItem.Keys thekey, boolean isDown) {
    	this.key = thekey;
    	this.isDown = isDown;
    }

	@Override
    public void decode(PacketByteBuf buf) {
    	this.key = KeypressItem.Keys.values()[buf.readInt()];
    	this.isDown = buf.readBoolean();
    }

	@Override
    public void encode(PacketByteBuf buf) {
    	buf.writeInt(this.key.ordinal());
    	buf.writeBoolean(this.isDown);
    }

	@Override
	public Identifier getChannel() {
		return GrappleMod.id("keypress");
	}

	@Override
    public void processMessage(NetworkContext ctx) {
    	final ServerPlayerEntity player = ctx.getSender();

		ctx.getServer().execute(() -> {
			if (player != null) {
				ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
				if (stack.getItem() instanceof KeypressItem keypressItem) {
					if (isDown) {
						keypressItem.onCustomKeyDown(stack, player, key, true);
					} else {
						keypressItem.onCustomKeyUp(stack, player, key, true);
					}

					return;
				}

				stack = player.getStackInHand(Hand.OFF_HAND);
				if (stack.getItem() instanceof KeypressItem keypressItem) {
					if (isDown) {
						keypressItem.onCustomKeyDown(stack, player, key, false);
					} else {
						keypressItem.onCustomKeyUp(stack, player, key, false);
					}
				}
			}
		});

	}
}
