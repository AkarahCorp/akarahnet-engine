package net.akarah.cdata.parsing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.akarah.cdata.codec.PaperCodecs;
import net.kyori.adventure.key.Key;

public interface RegistryHolder<T> {
    T get();

    static <T> Codec<RegistryHolder<T>> codec(ResourceRegistry<T> registry) {
        return Codec.withAlternative(
                holderCodec(registry),
                referenceCodec(registry)
        );
    }

    static <T> Codec<RegistryHolder<T>> referenceCodec(ResourceRegistry<T> registry) {
        return PaperCodecs.KEY.flatXmap(key -> DataResult.success(new Reference<>(key, registry)), ref -> {
            try {
                return DataResult.success(((Reference<T>) ref).key());
            } catch (Exception e) {
                return DataResult.error(() -> "not a ref");
            }
        });
    }

    static <T> Codec<RegistryHolder<T>> holderCodec(ResourceRegistry<T> registry) {
        return registry.codec.xmap(Direct::new, RegistryHolder::get);
    }

    record Reference<T>(Key key, ResourceRegistry<T> registry) implements RegistryHolder<T> {
        @Override
        public T get() {
            return registry.get(key).orElseThrow();
        }
    }

    record Direct<T>(T value) implements RegistryHolder<T> {
        @Override
        public T get() {
            return value;
        }
    }
}
