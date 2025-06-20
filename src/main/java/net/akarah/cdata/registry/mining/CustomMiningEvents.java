package net.akarah.cdata.registry.mining;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.akarah.cdata.Engine;
import net.akarah.cdata.Registries;
import net.akarah.cdata.registry.stat.StatsObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class CustomMiningEvents implements Listener {
    Map<UUID, Location> targetBlock = new HashMap<>();
    Map<UUID, Double> miningProgress = new HashMap<>();
    static Location DEFAULT_LOC = new Location(null, 0.0, 0.0, 0.0);

    static List<Vector> VECTORS;

    static {
        VECTORS = new ArrayList<>();

        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                for(int z = -1; z <= 1; z++) {
                    if(x != 0 ^ y != 0 ^ z != 0) {
                        var vec = new Vector(x, y, z);
                        if(!VECTORS.contains(vec)) {
                            VECTORS.add(vec);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void blockDamage(PlayerArmSwingEvent event) {
        event.setCancelled(true);

        var player = event.getPlayer();
        var uuid = player.getUniqueId();

        var oldTarget = targetBlock.getOrDefault(uuid, DEFAULT_LOC).toCenterLocation();
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
        Registries.MINING_RULE.forEach((name, rule) -> {
            if(rule.blockTypes().contains(newTarget.getBlock().getType().asBlockType())) {
                usedRule.set(rule);
            }
        });

        if(usedRule.get() == null) {
            return;
        }

        var rule = usedRule.get();
        var stats = Engine.statManager().getEntityStats(player);
        var miningTime = rule.timeForSpeed(stats.get(rule.speedStat()));

        if(currentProgress >= miningTime) {
            breakBlockFor(player, rule, newTarget, stats);
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

    public void breakBlockFor(Player p, BlockMiningRule rule, Location blockPos, StatsObject stats) {
        executeBlockBreak(rule, blockPos);

        rule.spreadStat().ifPresent(spreadStat -> {
            var spreadRemainder = stats.get(spreadStat.key());
            Bukkit.getGlobalRegionScheduler().runDelayed(Engine.get(), task -> {
                breakRecursively(p, rule, blockPos, spreadRemainder);
            }, 1);
        });
    }

    public void executeBlockBreak(BlockMiningRule rule, Location blockPos) {
        blockPos.getBlock().setType(Material.AIR);

//        rule.lootTable().ifPresent(lootTable -> {
//            for(var item : lootTable.get().roll()) {
//                blockPos.getWorld().dropItem(
//                        blockPos,
//                        item
//                );
//            }
//        });
    }

    public void breakRecursively(Player p, BlockMiningRule rule, Location blockPos, double spreadRemainder) {
        if(Math.random() <= spreadRemainder) {
            executeRecursiveBreak(p, rule, blockPos, spreadRemainder);
        }
    }

    public void executeRecursiveBreak(Player p, BlockMiningRule rule, Location blockPos, double spreadRemainder) {
        System.out.println(blockPos + " @ " + spreadRemainder);

        var tr = spreadRemainder;
        var positionsToBreak = new ArrayList<Location>();
        executeBlockBreak(rule, blockPos);

        Collections.shuffle(VECTORS);

        for(var vector : VECTORS) {
            var shiftedPos = blockPos.clone().add(vector);
            var material = shiftedPos.getBlock().getType().asBlockType();
            if(rule.blockTypes().contains(material) && Math.random() < tr) {
                positionsToBreak.add(shiftedPos);
                tr -= 1;
            }
        }

        for(var shiftedPos : positionsToBreak) {
            var rm = tr;
            Bukkit.getGlobalRegionScheduler().runDelayed(
                    Engine.get(),
                    task -> breakRecursively(p, rule, shiftedPos, rm),
                    1
            );
        }
    }
}
