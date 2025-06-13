package net.akarah.cdata.registry.stat;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.akarah.cdata.Registries;
import net.akarah.cdata.codec.PaperCodecs;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class StatsObject {
    public static Codec<StatsObject> CODEC = Codec.unboundedMap(PaperCodecs.KEY, Codec.DOUBLE)
            .xmap(StatsObject::new, x -> x.map);

    Map<Key, Double> map;

    public static StatsObject empty() {
        return new StatsObject(new HashMap<>());
    }

    public static StatsObject defaulted() {
        var statsObject = StatsObject.empty();
        Registries.CUSTOM_STAT.forEach((key, stat) -> {
            statsObject.set(key, stat.base());
        });
        return statsObject;
    }

    private StatsObject(Map<Key, Double> map) {
        this.map = map;
    }

    public double get(Key stat) {
        return this.map.getOrDefault(stat, 0.0);
    }

    public StatsObject set(Key stat, double value) {
        this.map.put(stat, value);
        return this;
    }

    public StatsObject add(Key stat, double value) {
        return this.set(stat, this.get(stat) + value);
    }

    public Set<Key> keySet() {
        return this.map.keySet();
    }

    public StatsObject add(StatsObject other) {
        var keySet = Sets.union(
                this.keySet(),
                other.keySet()
        );

        for(var key : keySet) {
            this.add(key, other.get(key));
        }

        return this;
    }

    public StatsObject copy() {
        return StatsObject.empty().add(this);
    }

    public void applyAttributes(LivingEntity entity) {
        for(var statKey : this.keySet()) {
            Registries.CUSTOM_STAT.get(statKey)
                    .flatMap(CustomStat::effects)
                    .ifPresent(effects -> {
                        effects.attribute().ifPresent(attributeKey -> {
                            if(effects.applyToOnlyEntities() && entity instanceof Player) {
                                return;
                            }

                            var attribute = Registry.ATTRIBUTE.get(attributeKey);
                            assert attribute != null;
                            entity.registerAttribute(attribute);
                            Objects.requireNonNull(entity.getAttribute(attribute))
                                    .setBaseValue(
                                            effects.base()
                                            + (effects.factor() * this.get(statKey))
                                    );
                        });
                    });
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append('[');
        for(var key : this.keySet()) {
            sb.append(key.toString())
                    .append('=')
                    .append(this.get(key))
                    .append(";");
        }
        sb.append(']');
        return sb.toString();
    }
}
