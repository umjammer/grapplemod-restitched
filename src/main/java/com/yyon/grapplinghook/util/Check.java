package com.yyon.grapplinghook.util;

import com.yyon.grapplinghook.GrappleMod;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Check {

    public static boolean missingTileEntity(BlockEntity blockEntity, PlayerEntity player, World level, BlockPos pos) {
        if(blockEntity == null) {
            player.sendMessage(Text.literal("Uh oh! Something went wrong. Check the server log.").formatted(Formatting.RED));
            GrappleMod.LOGGER.warn(String.format(
                    "Missing a tile entity for BlockGrappleModifier @ %s (%s,%s,%s)",
                    level.getRegistryKey(),
                    pos.getX(), pos.getZ(), pos.getZ()
            ));

            return true;
        }

        return false;
    }
}
