package net.akarah.cdata.mining;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.akarah.cdata.codec.PaperCodecs;
import net.kyori.adventure.key.Key;
import org.bukkit.block.BlockType;

import java.util.List;

public record BlockMiningRule(
        List<BlockType> blockTypes,
        double hardness,
        Key speedStat
) {
    public static Codec<BlockMiningRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PaperCodecs.BLOCK_TYPE.listOf().fieldOf("block_types").forGetter(BlockMiningRule::blockTypes),
            Codec.DOUBLE.fieldOf("hardness").forGetter(BlockMiningRule::hardness),
            PaperCodecs.KEY.fieldOf("speed_stat").forGetter(BlockMiningRule::speedStat)
    ).apply(instance, BlockMiningRule::new));

    // borrow from SkyBlock's formula for now
    public double timeForSpeed(double breakingSpeed) {
        return Math.round((30 * this.hardness) / breakingSpeed);
    }
}
