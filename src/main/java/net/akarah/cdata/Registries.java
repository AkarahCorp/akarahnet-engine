package net.akarah.cdata;

import net.akarah.cdata.item.CustomItem;
import net.akarah.cdata.parsing.ResourceRegistry;
import net.kyori.adventure.key.Key;

public class Registries {
    public static void init() {}

    public static ResourceRegistry<CustomItem> CUSTOM_ITEM = Engine.resources().register(
            Key.key("minecraft", "item"),
            ResourceRegistry.create(CustomItem.CODEC)
    );
}
