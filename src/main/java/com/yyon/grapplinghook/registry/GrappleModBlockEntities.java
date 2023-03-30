package com.yyon.grapplinghook.registry;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.blockentity.GrappleModifierBlockEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class GrappleModBlockEntities {

    private static HashMap<Identifier, GrappleModBlockEntities.BlockEntityEntry<?>> blockEntities;

    static {
        GrappleModBlockEntities.blockEntities = new HashMap<>();
    }

    public static <E extends BlockEntityType<?>> BlockEntityEntry<E> blockEntity(String id, Supplier<E> type) {
        Identifier qualId = GrappleMod.id(id);
        BlockEntityEntry<E> entry = new BlockEntityEntry<>(qualId, type);
        GrappleModBlockEntities.blockEntities.put(qualId, entry);
        return entry;
    }


    public static void registerAllBlockEntities() {
        for(Map.Entry<Identifier, BlockEntityEntry<?>> def: blockEntities.entrySet()) {
            Identifier id = def.getKey();
            BlockEntityEntry<?> data = def.getValue();
            BlockEntityType<?> it = data.getFactory().get();

            data.finalize(Registry.register(Registries.BLOCK_ENTITY_TYPE, id, it));
        }
    }

    public static final BlockEntityEntry<BlockEntityType<GrappleModifierBlockEntity>> GRAPPLE_MODIFIER = GrappleModBlockEntities
            .blockEntity("block_grapple_modifier",() -> BlockEntityType.Builder
                    .create(GrappleModifierBlockEntity::new, GrappleModBlocks.GRAPPLE_MODIFIER.get())
                    .build(null));


    public static class BlockEntityEntry<T extends BlockEntityType<?>> extends AbstractRegistryReference<T> {

        protected BlockEntityEntry(Identifier id, Supplier<T> factory) {
            super(id, factory);
        }
    }

}


