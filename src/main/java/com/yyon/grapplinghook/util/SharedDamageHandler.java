package com.yyon.grapplinghook.util;

import com.yyon.grapplinghook.entity.grapplehook.GrapplehookEntity;
import com.yyon.grapplinghook.item.GrapplehookItem;
import com.yyon.grapplinghook.item.LongFallBoots;
import com.yyon.grapplinghook.network.clientbound.GrappleDetachMessage;
import com.yyon.grapplinghook.server.ServerControllerManager;
import com.yyon.grapplinghook.util.GrappleModUtils;
import java.util.HashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class SharedDamageHandler {

    /** @return true if the death should be cancelled. */
    public static boolean handleDeath(Entity deadEntity) {
        if (!deadEntity.world.isClient) {
            int id = deadEntity.getId();
            boolean isConnected = ServerControllerManager.allGrapplehookEntities.containsKey(id);

            if (isConnected) return false;

            HashSet<GrapplehookEntity> grapplehookEntities = ServerControllerManager.allGrapplehookEntities.get(id);

            if(grapplehookEntities != null) {
                for (GrapplehookEntity hookEntity : grapplehookEntities)
                    hookEntity.removeServer();

                grapplehookEntities.clear();
            }

            ServerControllerManager.attached.remove(id);

            GrapplehookItem.grapplehookEntitiesLeft.remove(deadEntity);
            GrapplehookItem.grapplehookEntitiesRight.remove(deadEntity);

            if(deadEntity instanceof PlayerEntity)
                GrappleModUtils.sendToCorrectClient(new GrappleDetachMessage(id), id, deadEntity.world);
        }

        return false;
    }

    /** @return true if the death should be cancelled. */
    public static boolean handleDamage(Entity damagedEntity, DamageSource source) {
        if (damagedEntity instanceof PlayerEntity player) {

            for (ItemStack armor : player.getArmorItems()) {
                if (armor != null && armor.getItem() instanceof LongFallBoots) continue;
                if (source.isOf(DamageTypes.FLY_INTO_WALL)) return true;
            }
        }

        return false;
    }
}
