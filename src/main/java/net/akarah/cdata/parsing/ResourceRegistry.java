package net.akarah.cdata.parsing;

import com.mojang.serialization.Codec;
import net.kyori.adventure.key.Key;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResourceRegistry<T> {
    Map<Key, T> innerMap = new HashMap<>();
    Codec<T> codec;

    public static <T> ResourceRegistry<T> create(Codec<T> codec) {
        var rr = new ResourceRegistry<T>();
        rr.codec = codec;
        return rr;
    }

    public void insert(Key key, T value) {
        this.innerMap.put(key, value);
    }

    public Optional<T> get(Key key) {
        return Optional.ofNullable(this.innerMap.get(key));
    }

    public Codec<T> valueCodec() {
        return this.codec;
    }

    @Override
    public String toString() {
        return this.innerMap.toString();
    }
}
