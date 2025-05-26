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
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ServerResources {
    Map<Key, ResourceRegistry<?>> registries = new HashMap<>();

    public <T> ResourceRegistry<T> register(Key key, ResourceRegistry<T> registry) {
        this.registries.put(key, registry);
        return registry;
    }

    public void loadFromFiles() {
        System.out.println("Loading from files!");
        var rootPath = Paths.get("./cdata/");

        try (var pathStream = Files.walk(rootPath)) {
            pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .filter(path -> rootPath.relativize(path).getNameCount() >= 3)
                    .forEach(this::loadFromPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void loadFromPath(Path path) {
        System.out.println(path);

        var cdataName = path.getName(1).toString();
        assert cdataName.equals("cdata");

        @Subst("minecraft") var namespace = path.getName(2).toString();
        @Subst("unknown") var registry = path.getName(3).toString();

        var registryKey = Key.key("minecraft", registry);

        @SuppressWarnings("unchecked")
        var dataRegistry = (ResourceRegistry<T>) this.registries.get(registryKey);

        if(dataRegistry == null) {
            Engine.logger().error("Registry {} does not exist, skipping.", registryKey);
            return;
        }
        var registryCodec = dataRegistry.valueCodec();

        try {
            var json = new Gson().fromJson(Files.readString(path), JsonElement.class);
            var encoded = registryCodec.decode(JsonOps.INSTANCE, json);

            var subpath = path.subpath(4, path.getNameCount());
            @Subst("empty") var keyValue = subpath.toString().replace(".json", "");

            var entryKey = Key.key(namespace, keyValue);

            if(encoded.isError()) {
                Engine.logger().error("Error occurred while loading resource {} / {}", registryKey, entryKey);
                Engine.logger().error(encoded.error().orElseThrow().message());
                return;
            }
            var encodedFinal = encoded.getOrThrow().getFirst();

            dataRegistry.insert(entryKey, encodedFinal);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
