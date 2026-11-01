package com.yyon.grapplinghook.client.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.client.physics.ClientPhysicsControllerTracker;
import com.yyon.grapplinghook.client.physics.controller.AirFrictionPhysicsController;
import com.yyon.grapplinghook.client.physics.controller.ForcefieldPhysicsController;
import com.yyon.grapplinghook.client.physics.controller.GrapplingHookPhysicsController;
import com.yyon.grapplinghook.content.customization.data.HookCustomization;
import com.yyon.grapplinghook.content.customization.type.BooleanProperty;
import com.yyon.grapplinghook.content.item.GrapplehookItem;
import com.yyon.grapplinghook.content.registry.internal.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

import static com.yyon.grapplinghook.content.registry.CustomizationProperties.*;

/**
 * Replaces the pre-1.21.4 ItemProperties predicates (rocket, double, motor, ...)
 * with a single conditional item model property, used from assets/grapplemod/items/*.json as
 * <pre>{ "type": "minecraft:condition", "property": "grapplemod:state", "state": "rocket", ... }</pre>
 */
@Environment(EnvType.CLIENT)
public record GrappleItemStateProperty(State state) implements ConditionalItemModelProperty {

    public static final MapCodec<GrappleItemStateProperty> MAP_CODEC = State.CODEC
            .fieldOf("state")
            .xmap(GrappleItemStateProperty::new, GrappleItemStateProperty::state);

    public static void register() {
        ConditionalItemModelProperties.ID_MAPPER.put(GrappleMod.id("state"), MAP_CODEC);
    }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return switch (this.state) {
            case ROCKET -> customization(stack, ROCKET_ATTACHED::get);
            case DOUBLE -> customization(stack, DOUBLE_HOOK_ATTACHED::get);
            case MOTOR -> customization(stack, MOTOR_ATTACHED::get);
            case ENDER_STAFF -> customization(stack, ENDER_STAFF_ATTACHED::get);
            case MAGNET -> customization(stack, MAGNET_ATTACHED::get);
            case HOOK_ONLY -> ModItems.GRAPPLING_HOOK.get().shouldDisplayAsHookOnly(stack);
            case ATTACHED -> {
                if (owner == null) yield false;
                ClientPhysicsControllerTracker tracker = GrappleModClient.get().getClientControllerManager();
                GrapplingHookPhysicsController controller = tracker.controllers.get(owner.getId());
                yield controller != null && !(controller instanceof AirFrictionPhysicsController);
            }
            case FORCEFIELD_ATTACHED -> {
                if (owner == null) yield false;
                ClientPhysicsControllerTracker tracker = GrappleModClient.get().getClientControllerManager();
                yield tracker.controllers.get(owner.getId()) instanceof ForcefieldPhysicsController;
            }
        };
    }

    private static boolean customization(ItemStack stack, Supplier<BooleanProperty> property) {
        if (!(stack.getItem() instanceof GrapplehookItem hook)) return false;
        HookCustomization volume = hook.getCustomizationsOrDefault(stack);
        return volume.get(property.get());
    }

    @Override
    public MapCodec<GrappleItemStateProperty> type() {
        return MAP_CODEC;
    }

    public enum State implements StringRepresentable {
        ROCKET("rocket"),
        DOUBLE("double"),
        MOTOR("motor"),
        ENDER_STAFF("enderstaff"),
        MAGNET("magnet"),
        ATTACHED("attached"),
        FORCEFIELD_ATTACHED("forcefield_attached"),
        HOOK_ONLY("hook");

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);

        private final String name;

        State(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
