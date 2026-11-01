package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.content.item.type.IDropHandling;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Player#drop(ItemStack, boolean, boolean) became LivingEntity#drop(ItemStack, boolean, Prediction),
// which only creates the item entity server-side. Hook the ServerPlayer override once a drop actually happened.
@Mixin(ServerPlayer.class)
public class PlayerDropHandlerMixin {

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZLnet/minecraft/util/Prediction;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("RETURN"))
    public void handleDrop(ItemStack droppedItem, boolean thrownFromHand, Prediction prediction, CallbackInfoReturnable<ItemEntity> cir) {
        if(cir.getReturnValue() == null) return;

        if(droppedItem.getItem() instanceof IDropHandling item) {
            item.onDroppedByPlayer(droppedItem, (ServerPlayer) (Object) this);
        }
    }

}
