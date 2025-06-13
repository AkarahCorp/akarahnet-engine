package net.akarah.cdata.registry.mining;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.akarah.cdata.Engine;
import net.akarah.cdata.Registries;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


public class CustomMiningEvents implements Listener {
    Map<UUID, Location> targetBlock = new HashMap<>();
    Map<UUID, Double> miningProgress = new HashMap<>();
    static Location DEFAULT_LOC = new Location(null, 0.0, 0.0, 0.0);

    @EventHandler
    public void blockDamage(PlayerArmSwingEvent event) {

        event.setCancelled(true);

        var player = event.getPlayer();
        var uuid = player.getUniqueId();

        var oldTarget = targetBlock.getOrDefault(uuid, DEFAULT_LOC).toCenterLocation();
        System.out.println(
                Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE))
                        .getValue()
        );
        var rayTraceResult = player.rayTraceBlocks(
                Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE))
                        .getValue()
        );
        if(rayTraceResult == null) {
            return;
        }
        assert rayTraceResult.getHitBlockFace() != null;
        var newVector = rayTraceResult.getHitPosition()
                .add(rayTraceResult.getHitBlockFace().getDirection().multiply(-0.5));
        var newTarget = new Location(
                player.getWorld(),
                newVector.getX(),
                newVector.getY(),
                newVector.getZ()
        ).toCenterLocation();

        int newSourceId = (int) (newTarget.getX() * newTarget.getY() * newTarget.getZ());
        int oldSourceId = (int) (oldTarget.getX() * oldTarget.getY() * oldTarget.getZ());

        if(!oldTarget.toCenterLocation().equals(newTarget)) {
            event.getPlayer().sendBlockDamage(
                    oldTarget,
                    0.0F,
                    event.getPlayer()
            );
            event.getPlayer().sendBlockDamage(
                    oldTarget,
                    0.0F,
                    oldSourceId
            );

            miningProgress.put(uuid, 0.0);
            targetBlock.put(uuid, newTarget);
        }

        double currentProgress = miningProgress.compute(uuid, (uuid2, a) -> {
            if(a == null) {
                return 0.0;
            }
            return a + 1;
        });

        AtomicReference<BlockMiningRule> usedRule = new AtomicReference<>();
        System.out.println(newTarget.getBlock().getType().asBlockType());
        Registries.MINING_RULE.forEach((name, rule) -> {
            if(rule.blockTypes().contains(newTarget.getBlock().getType().asBlockType())) {
                usedRule.set(rule);
            }
        });

        if(usedRule.get() == null) {
            return;
        }

        var rule = usedRule.get();
        var miningTime = rule.timeForSpeed(
                Engine.statManager().getEntityStats(player)
                        .get(rule.speedStat())
        );

        if(currentProgress >= miningTime) {
            newTarget.getBlock().breakNaturally();
        } else {
            event.getPlayer().sendBlockDamage(
                    newTarget,
                    0.0F,
                    event.getPlayer()
            );
            event.getPlayer().sendBlockDamage(
                    newTarget,
                    (float) (currentProgress / miningTime),
                    newSourceId
            );
        }
    }
}
