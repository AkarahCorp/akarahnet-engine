package net.akarah.cdata.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BlocksAttacks;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.akarah.cdata.codec.EnumCodec;
import net.akarah.cdata.codec.PaperCodecs;
import net.akarah.cdata.stat.StatsObject;
import net.akarah.cdata.util.Colors;
import net.akarah.cdata.util.Formatters;
import net.akarah.cdata.Registries;
import net.akarah.cdata.parsing.RegistryElement;
import net.akarah.cdata.parsing.ResourceRegistry;
import net.akarah.cdata.util.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record CustomItem(
        Material type,
        Component name,
        Optional<Rarity> rarity,
        Optional<String> description,
        Optional<String> clickAction,
        Optional<StatsObject> stats,
        boolean statsInOffHand
) implements RegistryElement<CustomItem> {
    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PaperCodecs.MATERIAL.fieldOf("type").forGetter(CustomItem::type),
            PaperCodecs.MINI_MESSAGE_COMPONENT.fieldOf("name").forGetter(CustomItem::name),
            EnumCodec.of(Rarity.class).optionalFieldOf("rarity").forGetter(CustomItem::rarity),
            Codec.STRING.optionalFieldOf("description").forGetter(CustomItem::description),
            Codec.STRING.optionalFieldOf("click_action").forGetter(CustomItem::clickAction),
            StatsObject.CODEC.optionalFieldOf("stats").forGetter(CustomItem::stats),
            Codec.BOOL.optionalFieldOf("stats_in_offhand", false).forGetter(CustomItem::statsInOffHand)
    ).apply(instance, CustomItem::new));

    public ItemStack toItemStack() {
        var is = ItemStack.of(this.type);
        is.setData(DataComponentTypes.ITEM_NAME, this.name);

        var lore = OutputLore.empty();
        this.description.ifPresent(description -> {
            lore.addLine(Component.empty());
            var descriptionLines = Formatters.splitIntoLines(description, 40);
            for(var line : descriptionLines) {
                lore.addLine(Component.text(line).color(Colors.LIGHT_GRAY));
            }
        });

        this.stats.ifPresent(stats -> {
            lore.addLine(Component.empty());
            for(var stat : stats.keySet()) {
                var customStat = Registries.CUSTOM_STAT.get(stat).orElseThrow();

                var line = Component.text(customStat.name() + ": ").color(TextColor.color(133, 133, 133));
                var value = stats.get(stat);
                if(value > 0) {
                    line = line.append(Component.text("+" + value).color(TextColor.color(0, 255, 0)));
                } else {
                    line = line.append(Component.text("-" + value).color(TextColor.color(255, 0, 0)));
                }

                lore.addLine(line);
            }
        });

        this.rarity.ifPresent(rarity -> {
            lore.addLine(Component.empty());
            lore.addLine(Component.text(Formatters.toSmallCaps(rarity.name().toLowerCase())).color(rarity.color));
        });

        this.clickAction.ifPresent(clickAction -> {
            lore.addLine(Component.empty());
            lore.addLine(Component.text(clickAction).color(Colors.YELLOW));
        });

        is.setData(DataComponentTypes.LORE, lore.build());

        is.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .hiddenComponents(Set.of(DataComponentTypes.ATTRIBUTE_MODIFIERS))
                .build());

        if(this.type.name().contains("SWORD")) {
            is.setData(DataComponentTypes.BLOCKS_ATTACKS, BlocksAttacks.blocksAttacks().damageReductions(List.of()).build());
        }

        is.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes()
                .addModifier(Attribute.ATTACK_SPEED, new AttributeModifier(
                        new NamespacedKey("akarahnet", "base"),
                        1000.0,
                        AttributeModifier.Operation.ADD_NUMBER,
                        EquipmentSlotGroup.ANY
                ))
                .build());

        is.editPersistentDataContainer(pdc -> pdc.set(Keys.ID, PersistentDataType.STRING, this.key().toString()));
        return is;
    }

    @Override
    public ResourceRegistry<CustomItem> registry() {
        return Registries.CUSTOM_ITEM;
    }
}
