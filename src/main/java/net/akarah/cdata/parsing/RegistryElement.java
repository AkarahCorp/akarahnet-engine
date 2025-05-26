package net.akarah.cdata.parsing;

import net.kyori.adventure.key.Key;

public interface RegistryElement<T extends RegistryElement<T>> {
    ResourceRegistry<T> registry();

    @SuppressWarnings("unchecked")
    default Key key() {
        return this.registry().getKey((T) this).orElseThrow();
    }
}
