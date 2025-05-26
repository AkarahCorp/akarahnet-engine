package net.akarah.cdata;

import net.akarah.cdata.parsing.ResourceRegistry;
import net.akarah.cdata.test.TestObject;
import net.kyori.adventure.key.Key;

public class Registries {
    public static void init() {}

    public static ResourceRegistry<TestObject> TEST_OBJS = Engine.resources().register(
            Key.key("minecraft", "test_obj"),
            ResourceRegistry.create(TestObject.CODEC)
    );
}
