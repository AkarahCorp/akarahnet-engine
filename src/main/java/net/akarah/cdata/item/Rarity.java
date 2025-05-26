package net.akarah.cdata.item;

import net.akarah.cdata.util.Colors;
import net.kyori.adventure.text.format.TextColor;

public enum Rarity {
    COMMON(Colors.WHITE),
    UNCOMMON(Colors.YELLOW),
    RARE(Colors.AQUA),
    EPIC(Colors.MAGENTA);

    final TextColor color;

    Rarity(TextColor color) {
        this.color = color;
    }
}
