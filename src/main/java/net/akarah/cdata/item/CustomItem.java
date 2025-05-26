package net.akarah.cdata.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.akarah.cdata.Registries;
import net.akarah.cdata.parsing.RegistryElement;
import net.akarah.cdata.parsing.ResourceRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record CustomItem(
        Material type,
        String name
) implements RegistryElement<CustomItem> {
    public static Codec<CustomItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING
                    .xmap(x -> Material.getMaterial(x.toUpperCase()), x -> x.name().toLowerCase())
                    .fieldOf("type").forGetter(CustomItem::type),
            Codec.STRING.fieldOf("name").forGetter(CustomItem::name)
    ).apply(instance, CustomItem::new));

    public ItemStack toItemStack() {
        var is = ItemStack.of(this.type);
        is.setData(DataComponentTypes.ITEM_NAME, Component.text(this.name));
        return is;
    }

    @Override
    public ResourceRegistry<CustomItem> registry() {
        return Registries.CUSTOM_ITEM;
    }
}
