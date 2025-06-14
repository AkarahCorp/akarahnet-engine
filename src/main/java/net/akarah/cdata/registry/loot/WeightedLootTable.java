package net.akarah.cdata.registry.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.akarah.cdata.Registries;
import net.akarah.cdata.codec.PaperCodecs;
import net.akarah.cdata.parsing.RegistryElement;
import net.akarah.cdata.parsing.ResourceRegistry;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record WeightedLootTable(
        List<LootEntry> entries,
        Optional<LootEntry> fallback,
        boolean endAfterRule
) implements RegistryElement<WeightedLootTable> {
    public static Codec<WeightedLootTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LootEntry.CODEC.listOf().fieldOf("entries").forGetter(WeightedLootTable::entries),
            LootEntry.CODEC.optionalFieldOf("fallback").forGetter(WeightedLootTable::fallback),
            Codec.BOOL.optionalFieldOf("end_after_rule", true).forGetter(WeightedLootTable::endAfterRule)
    ).apply(instance, WeightedLootTable::new));
    public record LootEntry(
            Key itemId,
            int amount,
            double weight
    ) {
        public static Codec<LootEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PaperCodecs.KEY.fieldOf("item_id").forGetter(LootEntry::itemId),
                Codec.INT.fieldOf("amount").forGetter(LootEntry::amount),
                Codec.DOUBLE.fieldOf("weight").forGetter(LootEntry::weight)
        ).apply(instance, LootEntry::new));
    }

    public double sumOfWeights() {
        return this.entries.stream()
                .mapToDouble(LootEntry::weight)
                .sum();
    }

    public List<ItemStack> roll() {
        var items = new ArrayList<ItemStack>();
        var random = Math.random() * this.sumOfWeights();
        for(var entry : this.entries) {
            random -= entry.weight;
            if(random <= 0.0) {
                var is = Registries.CUSTOM_ITEM.get(entry.itemId()).orElseThrow().toItemStack();
                is.setAmount(entry.amount);
                items.add(is);
                if(this.endAfterRule) {
                    return items;
                }
                random = Math.random() * this.sumOfWeights();
            }
        }
        return items;
    }

    @Override
    public ResourceRegistry<WeightedLootTable> registry() {
        return Registries.LOOT_TABLE;
    }
}
