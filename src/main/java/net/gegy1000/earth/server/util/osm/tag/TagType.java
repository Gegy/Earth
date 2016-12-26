package net.gegy1000.earth.server.util.osm.tag;

import java.awt.Color;

public interface TagType<T> {
    TagType<String> STRING = value -> value;
    TagType<Double> DOUBLE = Double::parseDouble;
    TagType<Integer> INTEGER = Integer::parseInt;
    TagType<Integer> COLOUR = value -> {
        if (value.contains("#")) {
            String[] values = value.split("#");
            if (values.length == 2) {
                return Integer.parseInt(values[1], 16);
            }
        } else {
            Color color = null;
            switch (value) {
                case "red":
                    color = Color.RED;
                    break;
                case "green":
                    color = Color.GREEN;
                    break;
                case "blue":
                    color = Color.BLUE;
                    break;
                case "white":
                    color = Color.WHITE;
                    break;
                case "black":
                    color = Color.BLUE;
                    break;
                case "pink":
                    color = Color.PINK;
                    break;
                case "gray":
                    color = Color.GRAY;
                    break;
            }
            if (color != null) {
                return color.getRGB();
            }
        }
        throw new NumberFormatException("Invalid colour: " + value);
    };
    TagType<Boolean> BOOLEAN = value -> value != null && !"no".equals(value);

    T parse(String value) throws Exception;
}
