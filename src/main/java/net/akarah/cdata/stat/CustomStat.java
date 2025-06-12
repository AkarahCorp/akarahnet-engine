package net.akarah.cdata.stat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.akarah.cdata.Registries;
import net.akarah.cdata.parsing.RegistryElement;
import net.akarah.cdata.parsing.ResourceRegistry;

import java.util.Optional;

public record CustomStat(
        String name,
        double base,
        Optional<StatEffects> effects
) implements RegistryElement<CustomStat> {
    public static Codec<CustomStat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(CustomStat::name),
            Codec.DOUBLE.optionalFieldOf("base", 0.0).forGetter(CustomStat::base),
            StatEffects.CODEC.optionalFieldOf("effects").forGetter(CustomStat::effects)
    ).apply(instance, CustomStat::new));

    @Override
    public ResourceRegistry<CustomStat> registry() {
        return Registries.CUSTOM_STAT;
    }
}
