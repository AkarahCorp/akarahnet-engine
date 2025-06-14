package net.akarah.cdata.registry.mining;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.akarah.cdata.Registries;
import net.akarah.cdata.codec.PaperCodecs;
import net.akarah.cdata.parsing.RegistryElement;
import net.akarah.cdata.parsing.RegistryHolder;
import net.akarah.cdata.parsing.ResourceRegistry;
import net.akarah.cdata.registry.loot.WeightedLootTable;
import net.kyori.adventure.key.Key;
import org.bukkit.block.BlockType;

import java.util.List;
import java.util.Optional;

public record BlockMiningRule(
        List<BlockType> blockTypes,
        double hardness,
        Key speedStat,
        Optional<RegistryHolder<WeightedLootTable>> lootTable
) implements RegistryElement<BlockMiningRule> {
    public static Codec<BlockMiningRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PaperCodecs.BLOCK_TYPE.listOf().fieldOf("block_types").forGetter(BlockMiningRule::blockTypes),
            Codec.DOUBLE.fieldOf("hardness").forGetter(BlockMiningRule::hardness),
            PaperCodecs.KEY.fieldOf("speed_stat").forGetter(BlockMiningRule::speedStat),
            RegistryHolder.codec(Registries.LOOT_TABLE).optionalFieldOf("loot_table").forGetter(BlockMiningRule::lootTable)
    ).apply(instance, BlockMiningRule::new));

    // borrow from SkyBlock's formula for now
    public double timeForSpeed(double breakingSpeed) {
        return Math.round((30 * this.hardness) / breakingSpeed);
    }

    @Override
    public ResourceRegistry<BlockMiningRule> registry() {
        return Registries.MINING_RULE;
    }
}
