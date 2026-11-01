package com.yyon.grapplinghook.content.registry.internal;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.content.registry.helper.AbstractRegistryReference;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ModBlocks {

    private static final HashMap<Identifier, BlockEntry<?>> blocks;

    static {
        blocks = new HashMap<>();
    }

    public static <B extends Block> Flow<B> block(String id, Supplier<B> block) {
        Identifier qualId = GrappleMod.id(id);
        BlockEntry<B> entry = new BlockEntry<>(qualId, block);
        blocks.put(qualId, entry);
        return new Flow<>(entry);
    }


    public static void registerAllBlocks() {
        for(Map.Entry<Identifier, BlockEntry<?>> def: blocks.entrySet()) {
            Identifier id = def.getKey();
            BlockEntry<?> data = def.getValue();
            Block it = data.getFactory().get();

            data.finalize(Registry.register(BuiltInRegistries.BLOCK, id, it));
        }
    }

    public static Map<Identifier, BlockEntry<?>> getBlocks() {
        return Collections.unmodifiableMap(blocks);
    }


    public static class Flow<B extends Block> {

        private final BlockEntry<B> context;

        public Flow(BlockEntry<B> context) {
            this.context = context;
        }

        public BlockEntry<B> define() {
            return this.context;
        }

        public Flow<B> withItem(Consumer<ModItems.ItemEntry<BlockItem>>  destination) {
            return this.withConfiguredItem(destination, UnaryOperator.identity());
        }

        public Flow<B> withConfiguredItem(Consumer<ModItems.ItemEntry<BlockItem>> destination, UnaryOperator<Item.Properties> properties) {
            return this.withCustomItem(destination, props -> new BlockItem(context.get(), properties.apply(props.useBlockDescriptionPrefix())));
        }

        public <I extends BlockItem> Flow<B> withCustomItem(Consumer<ModItems.ItemEntry<I>> destination, Function<Item.Properties, I> factory) {
            ModItems.ItemEntry<I> item = ModItems.item(context.getIdentifier().getPath(), factory, null, true);
            destination.accept(item);
            return this;
        }
    }

    public static class BlockEntry<B extends Block> extends AbstractRegistryReference<B> {
        protected BlockEntry(Identifier id, Supplier<B> factory) {
            super(id, factory);
        }
    }


    public static class BlockItemEntry<I extends BlockItem> extends ModItems.ItemEntry<I> implements Consumer<ModItems.ItemEntry<I>> {
        private ModItems.ItemEntry<I> source = null;

        protected BlockItemEntry() {
            super(null, () -> null, null);
        }

        @Override
        public void accept(ModItems.ItemEntry<I> item) {
            if(this.source != null) throw new IllegalStateException("The original item source cannot be defined more that once.");
            this.source = item;
        }

        @Override
        public Supplier<I> getFactory() {
            return this.source.getFactory();
        }

        @Override
        public I get() {
            return this.source.get();
        }

        @Override
        public void finalize(Object entry) {
            this.source.finalize(entry);
        }

        @Override
        public Identifier getIdentifier() {
            return this.source.getIdentifier();
        }

        public ModItems.ItemEntry<I> getSource() {
            return this.source;
        }
    }
}
