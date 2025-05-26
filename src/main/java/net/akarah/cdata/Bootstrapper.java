package net.akarah.cdata;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.akarah.cdata.parsing.ServerResources;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Bootstrapper implements PluginBootstrap {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        Engine.LOGGER = context.getLogger();
        Engine.RESOURCES = new ServerResources();
        Registries.init();
        Engine.resources().loadFromFiles();

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            var dispatcher = event.registrar();
             Registries.CUSTOM_ITEM.forEach(((key, customItem) -> dispatcher.register(
                     Commands.literal("cgive")
                             .then(
                                     Commands.literal(key.asString()).executes(ctx -> {
                                         var sender = ctx.getSource().getSender();
                                         if(sender instanceof Player p) {
                                             p.getInventory()
                                                     .addItem(customItem.toItemStack());
                                         }
                                         return 0;
                                     })
                             )
                             .build()
             )));
        });
    }
}
