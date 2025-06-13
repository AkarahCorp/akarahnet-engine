package net.akarah.cdata.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.akarah.cdata.Engine;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

public class PaperCodecs {
    public static Codec<Material> MATERIAL = Codec.STRING
            .xmap(x -> Material.getMaterial(x.toUpperCase()), x -> x.name().toLowerCase());

    public static Codec<World> WORLD = Codec.STRING.xmap(Bukkit::getWorld, WorldInfo::getName);

    public static Codec<BlockData> BLOCK_DATA = Codec.STRING.xmap(Bukkit::createBlockData, BlockData::getAsString);

    public static Codec<BlockState> BLOCK_STATE = PaperCodecs.BLOCK_DATA.xmap(
            BlockData::createBlockState,
            BlockState::getBlockData
    );

    public static Codec<Location> LOCATION = RecordCodecBuilder.create(instance -> instance.group(
            PaperCodecs.WORLD.fieldOf("world").forGetter(Location::getWorld),
            Codec.DOUBLE.fieldOf("x").forGetter(Location::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Location::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Location::z),
            Codec.FLOAT.fieldOf("pitch").forGetter(Location::getPitch),
            Codec.FLOAT.fieldOf("yaw").forGetter(Location::getYaw)
    ).apply(instance, Location::new));

    public static Codec<Vector> VECTOR = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vector::getX),
            Codec.DOUBLE.fieldOf("y").forGetter(Vector::getY),
            Codec.DOUBLE.fieldOf("z").forGetter(Vector::getZ)
    ).apply(instance, Vector::new));

    public static Codec<Component> MINI_MESSAGE_COMPONENT = Codec.STRING.xmap(Engine::mm, Engine::mm);

    public static Codec<Key> KEY = Codec.STRING.xmap(Key::key, Key::asString);

    public static Codec<EntityType> ENTITY_TYPE = PaperCodecs.KEY.xmap(
            Registry.ENTITY_TYPE::get,
            Keyed::key
    );
    public static Codec<BlockType> BLOCK_TYPE = PaperCodecs.KEY.xmap(
            Registry.BLOCK::get,
            Keyed::key
    );
}
