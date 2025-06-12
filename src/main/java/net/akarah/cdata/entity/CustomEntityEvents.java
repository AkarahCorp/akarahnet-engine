package net.akarah.cdata.entity;

import net.akarah.cdata.Engine;
import net.akarah.cdata.util.Keys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.persistence.PersistentDataType;

public class CustomEntityEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        var pdc = event.getEntity().getPersistentDataContainer();
        if(pdc.has(Keys.INVINCIBLE) && event.getCause() != EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if(event.getEntity() instanceof LivingEntity le) {
            Engine.statManager().set(
                    le.getUniqueId(),
                    Engine.statManager().getEntityStats(le)
            );
        }
    }
}
