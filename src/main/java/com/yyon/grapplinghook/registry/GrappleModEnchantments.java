package com.yyon.grapplinghook.registry;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.enchantment.DoubleJumpEnchantment;
import com.yyon.grapplinghook.enchantment.SlidingEnchantment;
import com.yyon.grapplinghook.enchantment.WallrunEnchantment;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GrappleModEnchantments {

    private static HashMap<Identifier, EnchantmentEntry<?>> enchantments;

    static {
        GrappleModEnchantments.enchantments = new HashMap<>();
    }

    public static <E extends Enchantment> EnchantmentEntry<E> enchantment(String id, Supplier<E> ench) {
        Identifier qualId = GrappleMod.id(id);
        EnchantmentEntry<E> entry = new EnchantmentEntry<>(qualId, ench);
        GrappleModEnchantments.enchantments.put(qualId, entry);
        return entry;
    }


    public static void registerAllEnchantments() {
        for(Map.Entry<Identifier, EnchantmentEntry<?>> def: enchantments.entrySet()) {
            Identifier id = def.getKey();
            EnchantmentEntry<?> data = def.getValue();
            Enchantment it = data.getFactory().get();

            data.finalize(Registry.register(Registries.ENCHANTMENT, id, it));
        }
    }

    public static final EnchantmentEntry<WallrunEnchantment> WALL_RUN = GrappleModEnchantments.enchantment("wallrunenchantment", WallrunEnchantment::new);
    public static final EnchantmentEntry<DoubleJumpEnchantment> DOUBLE_JUMP = GrappleModEnchantments.enchantment("doublejumpenchantment", DoubleJumpEnchantment::new);
    public static final EnchantmentEntry<SlidingEnchantment> SLIDING = GrappleModEnchantments.enchantment("slidingenchantment", SlidingEnchantment::new);

    public static class EnchantmentEntry<E extends Enchantment> extends AbstractRegistryReference<E> {
        protected EnchantmentEntry(Identifier id, Supplier<E> factory) {
            super(id, factory);
        }
    }
}
