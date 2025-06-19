package net.akarah.cdata.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.akarah.cdata.Engine;
import net.kyori.adventure.key.Key;
import org.intellij.lang.annotations.Subst;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ServerResources {
    Map<Key, ResourceRegistry<?>> registries = new HashMap<>();

    public <T> ResourceRegistry<T> register(Key key, ResourceRegistry<T> registry) {
        this.registries.put(key, registry);
        return registry;
    }

    public void loadFromFiles(Path rootPath) {
        try (var pathStream = Files.walk(rootPath)) {
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try {
                            var relativizedPath = rootPath.relativize(path);
                            var contents = Files.readString(path);
                            loadFromPath(relativizedPath, contents);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException ignored) {}
    }

    public <T> void loadFromPath(Path path, String contents) {
        @Subst("minecraft") var namespace = path.getName(0).toString();
        @Subst("unknown") var registry = path.getName(1).toString();

        var registryKey = Key.key("minecraft", registry);

        @SuppressWarnings("unchecked")
        var dataRegistry = (ResourceRegistry<T>) this.registries.get(registryKey);

        if(dataRegistry == null) {
            Engine.logger().error("Registry {} does not exist, skipping.", registryKey);
            return;
        }
        var registryCodec = dataRegistry.valueCodec();

        var json = new Gson().fromJson(contents, JsonElement.class);
        var encoded = registryCodec.decode(JsonOps.INSTANCE, json);

        var subpath = path.subpath(2, path.getNameCount());
        @Subst("empty") var keyValue = subpath.toString().replace(".json", "");

        var entryKey = Key.key(namespace, keyValue);

        if(encoded.isError()) {
            Engine.logger().error("Error occurred while loading resource {} / {}", registryKey, entryKey);
            Engine.logger().error(encoded.error().orElseThrow().message());
            return;
        }
        Engine.logger().info("Loaded {} into registry {}", entryKey, registryKey);
        var encodedFinal = encoded.getOrThrow().getFirst();

        dataRegistry.insert(entryKey, encodedFinal);
    }
}
