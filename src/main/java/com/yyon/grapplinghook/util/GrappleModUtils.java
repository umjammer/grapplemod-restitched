package com.yyon.grapplinghook.util;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.network.NetworkManager;
import com.yyon.grapplinghook.network.clientbound.BaseMessageClient;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class GrappleModUtils {

	private static int controllerid = 0;
	public static final int GRAPPLE_ID = controllerid++;
	public static final int REPEL_ID = controllerid++;
	public static final int AIR_FRICTION_ID = controllerid++;

	public static void sendToCorrectClient(BaseMessageClient message, int playerid, World w) {
		Entity entity = w.getEntityById(playerid);
		if (entity instanceof ServerPlayerEntity player) {
			NetworkManager.packetToClient(message, player);
		} else {
			GrappleMod.LOGGER.warn("ERROR! couldn't find player");
		}
	}

	public static BlockHitResult rayTraceBlocks(Entity entity, World world, Vec from, Vec to) {
		BlockHitResult result = world.raycast(new RaycastContext(from.toVec3d(), to.toVec3d(), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));

		return result.getType() == HitResult.Type.BLOCK
				? result
				: null;
	}

	public static long getTime(World w) {
		return w.getTime();
	}

	@SafeVarargs
	public static boolean and(Supplier<Boolean>... conditions) {
		boolean failed = Arrays.stream(conditions).anyMatch(bool -> !bool.get());
		return !failed;
	}

	public static boolean and(List<Supplier<Boolean>> conditions) {
		boolean failed = conditions.stream().anyMatch(bool -> !bool.get());
		return !failed;
	}

	public static synchronized ServerPlayerEntity[] getChunkPlayers(ServerWorld level, Vec point) {
		ChunkPos chunk = level.getWorldChunk(BlockPos.ofFloored(point.toVec3d())).getPos();
		return PlayerLookup.tracking(level, chunk).toArray(new ServerPlayerEntity[0]);
	}

	public static void registerPack(String id, Text displayName, ModContainer container, ResourcePackActivationType activationType) {
		ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(GrappleMod.MODID, id), container, displayName, activationType);
	}

}
