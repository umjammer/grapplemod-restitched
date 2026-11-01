package com.yyon.grapplinghook.content.registry.internal;

import com.yyon.grapplinghook.GrappleMod;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class ModGamerules {

    // Game rules are namespaced since 1.21.11: /gamerule grapplemod:use_limited_hook
    public static final GameRule<Boolean> USE_LIMITED_HOOK = GameRuleBuilder.forBoolean(false)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(GrappleMod.id("use_limited_hook"));

    // Run to ensure these are loaded.
    public static void bump() {}

}
