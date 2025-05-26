package net.akarah.cdata;

import net.akarah.cdata.parsing.ServerResources;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class Engine extends JavaPlugin {
    static ServerResources RESOURCES;
    static Engine INSTANCE;
    static Logger LOGGER;

    @Override
    public void onEnable() {
        Engine.INSTANCE = this;
    }

    public static Engine get() {
        return Engine.INSTANCE;
    }

    public static ServerResources resources() {
        return Engine.RESOURCES;
    }

    public static Logger logger() {
        return Engine.LOGGER;
    }
}
