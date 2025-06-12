package net.akarah.cdata;

import net.akarah.cdata.entity.CustomEntity;
import net.akarah.cdata.item.CustomItem;
import net.akarah.cdata.parsing.ResourceRegistry;
import net.akarah.cdata.stat.CustomStat;
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
}
