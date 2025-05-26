package net.akarah.cdata;

import net.akarah.cdata.parsing.ServerResources;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class Engine extends JavaPlugin {
    static ServerResources resources;
    static Engine INSTANCE;

    @Override
    public void onEnable() {
        Engine.INSTANCE = this;
        Engine.resources = new ServerResources();
        Registries.init();
        Engine.resources().loadFromFiles();
    }

    public static Engine getInstance() {
        return Engine.INSTANCE;
    }

    public static ServerResources resources() {
        return Engine.resources;
    }

    public static Logger logger() {
        return Engine.getInstance().getSLF4JLogger();
    }
}
