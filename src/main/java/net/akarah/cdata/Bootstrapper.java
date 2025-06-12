package net.akarah.cdata;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
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

            var root = Commands.literal("engine")
                    .requires(x -> x.getSender().isOp());

            var itemRoot = Commands.literal("give");
            Registries.CUSTOM_ITEM.forEach(((@Subst("minecraft:empty") var key, var customItem) -> {
                System.out.println("key; " + key + " custom; " + customItem);
                itemRoot.then(
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

            var entityRoot = Commands.literal("summon");
            Registries.CUSTOM_ENTITY.forEach(((@Subst("minecraft:empty") var key, var customEntity) -> {
                System.out.println("key; " + key + " custom; " + customEntity);
                entityRoot.then(
                        Commands.literal(Key.key(key.namespace(), key.value()).asString()).executes(ctx -> {
                            var sender = ctx.getSource().getSender();
                            if (sender instanceof Player p) {
                                customEntity.spawn(p.getLocation());
                            }
                            return 0;
                        })
                );
            }));

            root.then(
                    Commands.literal("stats_of").then(
                            Commands.argument("entity", ArgumentTypes.entity()).executes(ctx -> {
                                var entity = ctx.getArgument("entity", EntitySelectorArgumentResolver.class)
                                        .resolve(ctx.getSource())
                                        .getFirst();
                                if(ctx.getSource().getSender() instanceof Player p) {
                                    p.sendMessage(
                                            Engine.statManager().getEntityStats(entity)
                                                    .toString()
                                    );
                                }
                                return 0;
                            })
                    )
            );

            root.then(itemRoot);
            root.then(entityRoot);

            dispatcher.register(root.build());
        });
    }
}
