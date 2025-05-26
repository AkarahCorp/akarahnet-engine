package net.akarah.cdata.util;

import java.util.ArrayList;
import java.util.List;

public class Formatters {
    public static String toSmallCaps(String input) {
        var inputCharacters = "abcdefghijklmnopqrstuvwxyz";
        var outputCharacters = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

        var outputString = input;

        for(int i = 0; i<outputCharacters.length(); i++) {
            var inputChar = inputCharacters.charAt(i);
            var outputChar = outputCharacters.charAt(i);
            outputString = outputString.replace(inputChar, outputChar);
        }

        return outputString;
    }

    public static List<String> splitIntoLines(String input, int lineLength) {
        var descLines = new ArrayList<String>();
        var lineBuilder = new StringBuilder();

        for(var character : input.toCharArray()) {
            lineBuilder.append(character);
            if(Character.isWhitespace(character) && lineBuilder.length() >= lineLength) {
                descLines.add(lineBuilder.toString());
                lineBuilder.setLength(0);
            }
        }
        descLines.add(lineBuilder.toString());

        return descLines;
    }
}
