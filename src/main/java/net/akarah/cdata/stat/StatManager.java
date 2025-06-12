package net.akarah.cdata.stat;

import net.akarah.cdata.Engine;
import net.akarah.cdata.Registries;
import net.akarah.cdata.entity.CustomEntity;
import net.akarah.cdata.item.CustomItem;
import net.akarah.cdata.util.Keys;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.Subst;

import java.util.*;
import java.util.stream.Stream;

public class StatManager implements Listener {
    Map<UUID, StatsObject> stats = new HashMap<>();

    public StatsObject get(UUID uuid) {
        if(!stats.containsKey(uuid)) {
            stats.put(uuid, StatsObject.empty());
        }
        return stats.get(uuid);
    }

    public void set(UUID uuid, StatsObject stats) {
        this.stats.put(uuid, stats);
    }

    public StatsObject getEntityStats(Entity entity) {
        var statsObject = StatsObject.defaulted();

        var pdc = entity.getPersistentDataContainer();
        if(pdc.has(Keys.ID)) {
            @Subst("minecraft:empty") var id = pdc.get(Keys.ID, PersistentDataType.STRING);
            assert id != null;
            var key = Key.key(id);

            Registries.CUSTOM_ENTITY
                    .get(key)
                    .flatMap(CustomEntity::stats)
                    .ifPresent(statsObject::add);
        }

        if(entity instanceof Player p) {
            var items = List.of(
                p.getInventory().getItem(EquipmentSlot.HAND),
                p.getInventory().getItem(EquipmentSlot.HEAD),
                p.getInventory().getItem(EquipmentSlot.CHEST),
                p.getInventory().getItem(EquipmentSlot.LEGS),
                p.getInventory().getItem(EquipmentSlot.FEET)
            );

            for(var item : items) {
                if(!StatManager.hasItemId(item)) {
                    continue;
                }
                StatManager.getItemId(item).stats().ifPresent(statsObject::add);
            }
        }
        return statsObject;
    }

    public void updateEntityStats() {
        var keySet = this.stats.keySet();
        for(var uuid : keySet) {
            var entity = Bukkit.getEntity(uuid);
            if(entity == null) {
                this.stats.remove(uuid);
            }
        }

        for(var player : Bukkit.getOnlinePlayers()) {
            var stats = Engine.statManager().getEntityStats(player);
            stats.applyAttributes(player);
            this.stats.put(player.getUniqueId(), stats);

        }
    }

    private static boolean hasItemId(ItemStack itemStack) {
        return itemStack.getPersistentDataContainer().has(Keys.ID);
    }

    private static CustomItem getItemId(ItemStack itemStack) {
        @Subst("minecraft:empty") var id = itemStack.getPersistentDataContainer().get(Keys.ID, PersistentDataType.STRING);
        assert id != null;
        var key = Key.key(id);
        return Registries.CUSTOM_ITEM.get(key).orElseThrow();
    }
}
