package net.gegy1000.earth.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class EarthKeyBinds {
    public static final KeyBinding KEY_STREET_VIEW = new KeyBinding("Streetview", Keyboard.KEY_K, "The Earth Mod");
    public static final KeyBinding KEY_TELEPORT_PLACE = new KeyBinding("Teleport to Place", Keyboard.KEY_P, "The Earth Mod");
    public static final KeyBinding KEY_SHOW_MAP = new KeyBinding("Show Map", Keyboard.KEY_M, "The Earth Mod");

    public static void init() {
        ClientRegistry.registerKeyBinding(KEY_STREET_VIEW);
        ClientRegistry.registerKeyBinding(KEY_TELEPORT_PLACE);
        ClientRegistry.registerKeyBinding(KEY_SHOW_MAP);
    }
}
