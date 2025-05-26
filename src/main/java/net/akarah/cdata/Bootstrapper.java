package net.akarah.cdata;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.akarah.cdata.parsing.ServerResources;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

public class Bootstrapper implements PluginBootstrap {
    @Override
    @SuppressWarnings("unchecked")
    public void bootstrap(@NotNull BootstrapContext context) {
        Engine.LOGGER = context.getLogger();
        Engine.RESOURCES = new ServerResources();
        Registries.init();
        Engine.resources().loadFromFiles();

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var dispatcher = event.registrar();

            var root = Commands.literal("cgive");
            Registries.CUSTOM_ITEM.forEach(((@Subst("minecraft:empty") var key, var customItem) -> {
                System.out.println("key; " + key + " custom; " + customItem);
                root.then(
                        Commands.literal(Key.key(key.namespace(), key.value()).asString()).executes(ctx -> {
                            var sender = ctx.getSource().getSender();
                            if (sender instanceof Player p) {
                                p.getInventory()
                                        .addItem(customItem.toItemStack());
                            }
                            return 0;
                        })
                );
            }));
            dispatcher.register(root.build());
        });
    }
}
