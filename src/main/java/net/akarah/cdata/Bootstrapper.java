package net.akarah.cdata;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.datapack.DatapackSource;
import io.papermc.paper.datapack.DiscoveredDatapack;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.akarah.cdata.parsing.ServerResources;
import net.kyori.adventure.key.Key;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Bootstrapper implements PluginBootstrap {
    public static Map<String, DiscoveredDatapack> DISCOVERED_PACKS = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void bootstrap(@NotNull BootstrapContext context) {
        Engine.LOGGER = context.getLogger();

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, event -> {
            DISCOVERED_PACKS.putAll(event.registrar().getDiscoveredPacks());
        });

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.BLOCK), event -> {
            var list = new ArrayList<TagEntry<BlockType>>();
            for(var field : BlockTypeKeys.class.getDeclaredFields()) {
                try {
                    var value = (TypedKey<BlockType>) field.get(null);
                    list.add(TagEntry.valueEntry(value));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            event.registrar().setTag(
                    TagKey.create(RegistryKey.BLOCK, Key.key("akarahnet:all_blocks")),
                    list
            );
        });

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Engine.RESOURCES = new ServerResources();
            Registries.init();

            var properties = new Properties();
            try {
                properties.load(new FileInputStream("./server.properties"));
                for(var map : Bootstrapper.DISCOVERED_PACKS.values()) {
                    if (map.getSource().equals(DatapackSource.WORLD)) {
                        var renamed = map.getName().replace("file/", "");
                        var datapackFolderPath = Path.of(
                                "./"
                                        + properties.get("level-name")
                                        + "/datapacks/" + renamed + "/");
                        var dataContentsFolderPath = datapackFolderPath.resolve("./data/");
                        Engine.RESOURCES.loadFromFiles(dataContentsFolderPath);
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            var dispatcher = event.registrar();

            var root = Commands.literal("engine")
                    .requires(x -> x.getSender().isOp());

            var itemRoot = Commands.literal("give");
            Registries.CUSTOM_ITEM.forEach(((@Subst("minecraft:empty") var key, var customItem) -> {
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

            var lootTableRoot = Commands.literal("roll");
            Registries.LOOT_TABLE.forEach(((@Subst("minecraft:empty") var key, var lootTable) -> {
                lootTableRoot.then(
                        Commands.literal(Key.key(key.namespace(), key.value()).asString()).executes(ctx -> {
                            var sender = ctx.getSource().getSender();
                            if (sender instanceof Player p) {
                                for(var item : lootTable.roll()) {
                                    p.getInventory().addItem(item);
                                }
                            }
                            return 0;
                        })
                );
            }));

            var entityRoot = Commands.literal("summon");
            Registries.CUSTOM_ENTITY.forEach(((@Subst("minecraft:empty") var key, var customEntity) -> {
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
            root.then(lootTableRoot);

            dispatcher.register(root.build());
        });
    }
}
