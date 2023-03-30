package com.yyon.grapplinghook.mixin.client;

import com.yyon.grapplinghook.client.ClientControllerManager;
import com.yyon.grapplinghook.client.keybind.GrappleModKeyBindings;
import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.item.KeypressItem;
import com.yyon.grapplinghook.registry.GrappleModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientHookMixin {

    private static final boolean[] keyPressHistory = new boolean[]{ false, false, false, false, false };


    @Inject(method = "tick()V", at = @At("TAIL"))
    public void clientTickHook(CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            if (!MinecraftClient.getInstance().isPaused()) {
                ClientControllerManager.instance.onClientTick(player);

                if (MinecraftClient.getInstance().currentScreen == null) {
                    // keep in same order as enum from KeypressItem
                    boolean[] keys = {
                            GrappleModKeyBindings.key_enderlaunch.isPressed(), GrappleModKeyBindings.key_leftthrow.isPressed(),
                            GrappleModKeyBindings.key_rightthrow.isPressed(), GrappleModKeyBindings.key_boththrow.isPressed(),
                            GrappleModKeyBindings.key_rocket.isPressed()
                    };

                    for (int i = 0; i < keys.length; i++) {
                        boolean isKeyDown = keys[i];
                        boolean prevKey = ClientHookMixin.keyPressHistory[i];

                        if (isKeyDown != prevKey) {
                            KeypressItem.Keys key = KeypressItem.Keys.values()[i];

                            ItemStack stack = this.getKeypressStack(player);
                            if (stack != null) {
                                if (!this.isLookingAtModifierBlock(player)) {
                                    if (isKeyDown) {
                                        ((KeypressItem) stack.getItem()).onCustomKeyDown(stack, player, key, true);
                                    } else {
                                        ((KeypressItem) stack.getItem()).onCustomKeyUp(stack, player, key, true);
                                    }
                                }
                            }
                        }

                        ClientHookMixin.keyPressHistory[i] = isKeyDown;
                    }
                }
            }
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;reset()V"))
    public void handleLogOut(Screen pScreen, CallbackInfo ci) {
        GrappleConfig.setServerOptions(null);
    }


    public ItemStack getKeypressStack(PlayerEntity player) {
        if (player == null) return null;

        ItemStack stack;

        stack = player.getStackInHand(Hand.MAIN_HAND);
        if (stack.getItem() instanceof KeypressItem) return stack;

        stack = player.getStackInHand(Hand.OFF_HAND);
        if (stack.getItem() instanceof KeypressItem) return stack;

        return null;
    }

    public boolean isLookingAtModifierBlock(PlayerEntity player) {
        HitResult result = MinecraftClient.getInstance().crosshairTarget;
        if (result != null && result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult bray = (BlockHitResult) result;
            BlockPos pos = bray.getBlockPos();
            BlockState state = player.world.getBlockState(pos);

            return (state.getBlock() == GrappleModBlocks.GRAPPLE_MODIFIER.get());
        }
        return false;
    }
}
