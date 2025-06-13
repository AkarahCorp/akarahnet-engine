package net.akarah.cdata;

import net.akarah.cdata.entity.CustomEntityEvents;
import net.akarah.cdata.mining.CustomMiningEvents;
import net.akarah.cdata.parsing.ServerResources;
import net.akarah.cdata.stat.StatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class Engine extends JavaPlugin {
    static ServerResources RESOURCES;
    static Engine INSTANCE;
    static Logger LOGGER;
    static MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    static StatManager STAT_MANAGER = new StatManager();

    @Override
    public void onEnable() {
        Engine.INSTANCE = this;

        Bukkit.getServer().getPluginManager().registerEvents(new CustomEntityEvents(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new CustomMiningEvents(), this);
        Bukkit.getServer().getPluginManager().registerEvents(STAT_MANAGER, this);

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, task -> STAT_MANAGER.updateEntityStats(), 1, 5);
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

    public static MiniMessage miniMessage() {
        return Engine.MINI_MESSAGE;
    }

    public static Component mm(String text) {
        return Engine.miniMessage().deserialize(text);
    }

    public static String mm(Component input) {
        return Engine.miniMessage().serialize(input);
    }

    public static StatManager statManager() {
        return STAT_MANAGER;
    }
}
