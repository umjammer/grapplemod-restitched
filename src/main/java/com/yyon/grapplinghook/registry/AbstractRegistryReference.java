package com.yyon.grapplinghook.registry;

import java.util.function.Supplier;
import net.minecraft.util.Identifier;

public abstract class AbstractRegistryReference<T> {

    private final Identifier id;
    private final Supplier<T> factory;

    private T entry;

    protected AbstractRegistryReference(Identifier id, Supplier<T> factory) {
        this.id = id;
        this.factory = factory;

        this.entry = null;
    }

    Supplier<T> getFactory() {
        return this.factory;
    }

    @SuppressWarnings("unchecked")
    protected void finalize(Object entry) {
        if(this.entry != null) throw new IllegalStateException("Item is already registered!");

        try {
            this.entry = (T) entry;
        } catch (ClassCastException err) {
            throw new IllegalStateException("Item is already registered by a different mod!");
        }

    }


    public Identifier getIdentifier() {
        return this.id;
    }

    public T get() {
        return this.entry;
    }

    public boolean isRegistered() {
        return this.entry != null;
    }
}
