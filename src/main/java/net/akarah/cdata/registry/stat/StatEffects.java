package net.akarah.cdata.registry.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.akarah.cdata.codec.PaperCodecs;
import net.kyori.adventure.key.Key;

import java.util.Optional;

public record StatEffects(
        Optional<Key> attribute,
        double base,
        double factor,
        boolean applyToOnlyEntities
) {
    public static Codec<StatEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PaperCodecs.KEY.optionalFieldOf("attribute").forGetter(StatEffects::attribute),
            Codec.DOUBLE.optionalFieldOf("base", 0.0).forGetter(StatEffects::base),
            Codec.DOUBLE.optionalFieldOf("factor", 1.0).forGetter(StatEffects::factor),
            Codec.BOOL.optionalFieldOf("apply_to_only_entities", false).forGetter(StatEffects::applyToOnlyEntities)
    ).apply(instance, StatEffects::new));
}
