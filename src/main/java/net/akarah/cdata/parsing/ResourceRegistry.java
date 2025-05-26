package net.akarah.cdata.parsing;

import com.mojang.serialization.Codec;
import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class ResourceRegistry<T> {
    Map<Key, T> innerMap = new HashMap<>();
    Map<T, Key> inverseMap = new HashMap<>();
    Codec<T> codec;

    public static <T> ResourceRegistry<T> create(Codec<T> codec) {
        var rr = new ResourceRegistry<T>();
        rr.codec = codec;
        return rr;
    }

    public void insert(Key key, T value) {
        this.innerMap.put(key, value);
        this.inverseMap.put(value, key);
    }

    public Optional<T> get(Key key) {
        return Optional.ofNullable(this.innerMap.get(key));
    }

    public Optional<Key> getKey(T value) {
        return Optional.ofNullable(this.inverseMap.get(value));
    }

    public Codec<T> valueCodec() {
        return this.codec;
    }

    public void forEach(BiConsumer<Key, T> func) {
        this.innerMap.forEach(func);
    }

    @Override
    public String toString() {
        return this.innerMap.toString();
    }
}
