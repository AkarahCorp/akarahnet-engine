package net.akarah.cdata;

import net.akarah.cdata.registry.entity.CustomEntity;
import net.akarah.cdata.registry.item.CustomItem;
import net.akarah.cdata.registry.mining.BlockMiningRule;
import net.akarah.cdata.parsing.ResourceRegistry;
import net.akarah.cdata.registry.stat.CustomStat;
import net.kyori.adventure.key.Key;

public class Registries {
    public static void init() {}

    public static ResourceRegistry<CustomItem> CUSTOM_ITEM = Engine.resources().register(
            Key.key("minecraft", "item"),
            ResourceRegistry.create(CustomItem.CODEC)
    );

    public static ResourceRegistry<CustomEntity> CUSTOM_ENTITY = Engine.resources().register(
            Key.key("minecraft", "entity"),
            ResourceRegistry.create(CustomEntity.CODEC)
    );

    public static ResourceRegistry<CustomStat> CUSTOM_STAT = Engine.resources().register(
            Key.key("minecraft", "stat"),
            ResourceRegistry.create(CustomStat.CODEC)
    );

    public static ResourceRegistry<BlockMiningRule> MINING_RULE = Engine.resources().register(
            Key.key("minecraft", "mining_rule"),
            ResourceRegistry.create(BlockMiningRule.CODEC)
    );
}
