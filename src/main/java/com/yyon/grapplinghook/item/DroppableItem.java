package com.yyon.grapplinghook.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface DroppableItem {

    void onDroppedByPlayer(ItemStack item, PlayerEntity player);

}
