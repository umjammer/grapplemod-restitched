package com.yyon.grapplinghook.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.yyon.grapplinghook.GrappleMod;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import java.util.ArrayList;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class ModKeys {

    public static ArrayList<KeyMapping> keyBindings = new ArrayList<>();

    // Translated via "key.category.grapplemod.category".
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(GrappleMod.id("category"));

    public static KeyMapping createKeyBinding(KeyMapping k) {
        keyBindings.add(k);
        return k;
    }

    public static WithFallback createKeyBindingWithFallback(KeyMapping k, Function<Options, KeyMapping> vanillaFallback) {
        keyBindings.add(k);
        return new WithFallback(k, vanillaFallback);
    }

    public static final WithFallback THROW_HOOKS = ModKeys.createKeyBindingWithFallback(new KeyMapping("key.boththrow.desc", InputConstants.UNKNOWN.getValue(), CATEGORY), options -> options.keyUse);
    public static final WithFallback TOGGLE_MOTOR = ModKeys.createKeyBindingWithFallback(new KeyMapping("key.motoronoff.desc", InputConstants.UNKNOWN.getValue(), CATEGORY), options -> options.keyShift);
    public static final WithFallback DETACH = ModKeys.createKeyBindingWithFallback(new KeyMapping("key.jumpanddetach.desc", InputConstants.UNKNOWN.getValue(), CATEGORY), options -> options.keyJump);
    public static final WithFallback DAMPEN_SWING = ModKeys.createKeyBindingWithFallback(new KeyMapping("key.slow.desc", InputConstants.UNKNOWN.getValue(), CATEGORY), options -> options.keyShift);
    public static final WithFallback CLIMB = ModKeys.createKeyBindingWithFallback(new KeyMapping("key.climb.desc", InputConstants.UNKNOWN.getValue(), CATEGORY), options -> options.keyShift);
    public static final WithFallback HOOK_ENDER_LAUNCH = ModKeys.createKeyBindingWithFallback(new KeyMapping("key.enderlaunch.desc", InputConstants.UNKNOWN.getValue(), CATEGORY), options -> options.keyAttack);
    public static final WithFallback ROCKET = ModKeys.createKeyBindingWithFallback(new KeyMapping("key.rocket.desc", InputConstants.UNKNOWN.getValue(), CATEGORY), options -> options.keyAttack);

    public static final KeyMapping THROW_OFF_HOOK = ModKeys.createKeyBinding(new KeyMapping("key.off_throw.desc", InputConstants.UNKNOWN.getValue(), CATEGORY));
    public static final KeyMapping THROW_MAIN_HOOK = ModKeys.createKeyBinding(new KeyMapping("key.main_throw.desc", InputConstants.UNKNOWN.getValue(), CATEGORY));
    public static final KeyMapping CLIMB_UP = ModKeys.createKeyBinding(new KeyMapping("key.climbup.desc", InputConstants.UNKNOWN.getValue(), CATEGORY));
    public static final KeyMapping CLIMB_DOWN = ModKeys.createKeyBinding(new KeyMapping("key.climbdown.desc", InputConstants.UNKNOWN.getValue(), CATEGORY));


    public static void registerAll() {
        for(KeyMapping mapping: ModKeys.keyBindings) {
            KeyMappingHelper.registerKeyMapping(mapping);
        }
    }

    /**
     * A lot of this mod's custom keybinds clash with vanilla. For 1.21.1 - 1.21.8, the mod will use
     * a vanilla keybind for each key unless an alternative is bound.
     */
    public record WithFallback(KeyMapping modMapping, Function<Options, KeyMapping> vanillaFallback) {

        public KeyMapping get() {
            return this.modMapping.isUnbound()
                    ? this.vanillaFallback.apply(Minecraft.getInstance().options)
                    : this.modMapping;
        }

    }

}
