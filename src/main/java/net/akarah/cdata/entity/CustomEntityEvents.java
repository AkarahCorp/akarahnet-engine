package net.akarah.cdata.entity;

import net.akarah.cdata.util.Keys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

public class CustomEntityEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        var pdc = event.getEntity().getPersistentDataContainer();
        if(pdc.has(Keys.INVINCIBLE) && event.getCause() != EntityDamageEvent.DamageCause.VOID) {
            event.setCancelled(true);
            return;
        }
        if(pdc.has(Keys.HEALTH)) {
            var hp = pdc.get(Keys.HEALTH, PersistentDataType.DOUBLE);
            hp -= event.getDamage();
            if(hp <= 0) {
                event.getEntity().remove();
            } else {
                pdc.set(Keys.HEALTH, PersistentDataType.DOUBLE, hp);
            }

            event.setDamage(0.001);
            return;
        }
    }
}
