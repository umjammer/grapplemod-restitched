package com.yyon.grapplinghook.registry;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.entity.grapplehook.GrapplehookEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GrappleModEntities {

    private static HashMap<Identifier, EntityEntry<?>> entities;

    static {
        GrappleModEntities.entities = new HashMap<>();
    }

    public static <E extends EntityType<?>> EntityEntry<E> entity(String id, Supplier<E> type) {
        Identifier qualId = GrappleMod.id(id);
        EntityEntry<E> entry = new EntityEntry<>(qualId, type);
        GrappleModEntities.entities.put(qualId, entry);
        return entry;
    }


    public static void registerAllEntities() {
        for(Map.Entry<Identifier, EntityEntry<?>> def: entities.entrySet()) {
            Identifier id = def.getKey();
            EntityEntry<?> data = def.getValue();
            EntityType<?> it = data.getFactory().get();

            data.finalize(Registry.register(Registries.ENTITY_TYPE, id, it));
        }
    }

    public static final EntityEntry<EntityType<GrapplehookEntity>> GRAPPLE_HOOK = GrappleModEntities
            .entity("grapplehook", () -> FabricEntityTypeBuilder.<GrapplehookEntity>
                     create(SpawnGroup.MISC, GrapplehookEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .build()
            );



    public static class EntityEntry<T extends EntityType<?>> extends AbstractRegistryReference<T> {

        protected EntityEntry(Identifier id, Supplier<T> factory) {
            super(id, factory);
        }
    }

}


