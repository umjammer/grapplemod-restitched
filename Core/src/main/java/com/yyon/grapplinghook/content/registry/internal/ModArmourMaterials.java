package com.yyon.grapplinghook.content.registry.internal;

import com.yyon.grapplinghook.GrappleMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

import java.util.EnumMap;
import java.util.Map;

// yes, the correct british "armour".
// Armour materials are no longer registered; they are plain records used to build item components.
public class ModArmourMaterials {

    public static final TagKey<Item> REPAIRS_LONG_FALL_BOOTS = TagKey.create(Registries.ITEM, GrappleMod.id("repairs_long_fall_boots"));

    // Required by the ArmorMaterial record, but not applied to the item (see applyArmour).
    public static final ResourceKey<EquipmentAsset> LONG_FALL_BOOTS_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, GrappleMod.id("long_fall_boots"));

    public static final ArmorMaterial LONG_FALL_BOOTS = new ArmorMaterial(
            0, // durability multiplier - unused, the boots don't take durability damage.
            new EnumMap<>(Map.of(
                    ArmorType.HELMET, 0,
                    ArmorType.CHESTPLATE, 0,
                    ArmorType.LEGGINGS, 0,
                    ArmorType.BOOTS, 3,
                    ArmorType.BODY, 11
            )),
            10, // enchantmentValue
            SoundEvents.ARMOR_EQUIP_CHAIN,
            2.0f, 0.0f, // toughness, knockbackResistance
            REPAIRS_LONG_FALL_BOOTS,
            LONG_FALL_BOOTS_ASSET
    );

    /** Applies armour components like Item.Properties#humanoidArmor, minus durability. */
    public static Item.Properties applyArmour(Item.Properties properties, ArmorMaterial material, ArmorType type) {
        ItemAttributeModifiers attributes = material.createAttributes(type);
        return properties
                .attributes(attributes)
                .enchantable(material.enchantmentValue())
                .component(net.minecraft.core.component.DataComponents.EQUIPPABLE,
                        // No equipment asset: the 3D model is drawn by LongFallBootsRenderer instead.
                        Equippable.builder(type.getSlot())
                                .setEquipSound(material.equipSound())
                                .build())
                .repairable(material.repairIngredient());
    }

    // Kept for call-site compatibility; nothing needs registering anymore.
    public static void registerAllMaterials() {}

}
