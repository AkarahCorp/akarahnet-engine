package net.akarah.cdata.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TestObject(String string, int number) {
    public static Codec<TestObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("string").forGetter(TestObject::string),
            Codec.INT.fieldOf("number").forGetter(TestObject::number)
    ).apply(instance, TestObject::new));
}
