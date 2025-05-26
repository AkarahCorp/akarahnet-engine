package net.akarah.cdata.item;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class OutputLore {
    public List<Component> components = new ArrayList<>();

    public static OutputLore empty() {
        return new OutputLore();
    }

    public void addLine(Component component) {
        this.components.add(component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
    }

    public ItemLore build() {
        return ItemLore.lore(this.components);
    }
}
