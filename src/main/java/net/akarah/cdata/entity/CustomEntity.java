package net.akarah.cdata.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.akarah.cdata.Engine;
import net.akarah.cdata.Registries;
import net.akarah.cdata.codec.PaperCodecs;
import net.akarah.cdata.parsing.RegistryElement;
import net.akarah.cdata.parsing.ResourceRegistry;
import net.akarah.cdata.util.Keys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public record CustomEntity(
        EntityType entityType,
        Component name,
        Optional<Double> health,
        boolean invincible
) implements RegistryElement<CustomEntity> {
    public static Codec<CustomEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PaperCodecs.ENTITY_TYPE.fieldOf("type").forGetter(CustomEntity::entityType),
            PaperCodecs.MINI_MESSAGE_COMPONENT.fieldOf("name").forGetter(CustomEntity::name),
            Codec.DOUBLE.optionalFieldOf("health").forGetter(CustomEntity::health),
            Codec.BOOL.optionalFieldOf("invincible", false).forGetter(CustomEntity::invincible)
    ).apply(instance, CustomEntity::new));

    @Override
    public ResourceRegistry<CustomEntity> registry() {
        return Registries.CUSTOM_ENTITY;
    }

    public Entity spawn(Location location) {
        var mob = location.getWorld().spawnEntity(
                location,
                this.entityType
        );
        var id = this.registry().getKey(this).orElseThrow();

        mob.customName(this.name);


        var pdc = mob.getPersistentDataContainer();
        pdc.set(Keys.ID, PersistentDataType.STRING, id.value());
        this.health.ifPresent(value -> {
            pdc.set(Keys.HEALTH, PersistentDataType.DOUBLE, value);
        });
        if(this.invincible) {
            pdc.set(Keys.INVINCIBLE, PersistentDataType.BOOLEAN, true);
        }
        return mob;
    }
}
