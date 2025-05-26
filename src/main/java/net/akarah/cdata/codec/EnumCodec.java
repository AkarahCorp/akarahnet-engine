package net.akarah.cdata.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnumCodec<E extends Enum<E>> implements Codec<E> {
    Class<E> enumValue;

    Method enumValueOfMethod;

    public static <E extends Enum<E>> EnumCodec<E> of(Class<E> value) {
        var ec = new EnumCodec<E>();
        ec.enumValue = value;
        try {
            ec.enumValueOfMethod = value.getDeclaredMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return ec;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
        return ops.getStringValue(input)
                .flatMap(string -> {
                    try {
                        return DataResult.success(new Pair<>(
                                (E) enumValueOfMethod.invoke(null, string.toUpperCase()),
                                input
                        ));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return DataResult.error(e::getMessage);
                    }
                });
    }

    @Override
    public <T> DataResult<T> encode(E input, DynamicOps<T> ops, T prefix) {
        return DataResult.success(ops.createString(input.name().toLowerCase()));
    }
}
