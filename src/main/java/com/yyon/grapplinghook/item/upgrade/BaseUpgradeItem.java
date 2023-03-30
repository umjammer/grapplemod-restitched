package com.yyon.grapplinghook.item.upgrade;

import com.yyon.grapplinghook.util.GrappleCustomization;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;

public class BaseUpgradeItem extends Item {
	public GrappleCustomization.UpgradeCategories category = null;

	public BaseUpgradeItem() {
		this(64, null);
	}

	public BaseUpgradeItem(int maxStackSize, GrappleCustomization.UpgradeCategories theCategory) {
		super(new Item.Settings().maxCount(maxStackSize));
		
		this.category = theCategory;
	}
}
