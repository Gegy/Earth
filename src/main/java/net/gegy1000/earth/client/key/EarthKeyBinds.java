package net.gegy1000.earth.client.key;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class EarthKeyBinds
{
    public static KeyBinding key_streetview = new KeyBinding("Streetview", Keyboard.KEY_K, "The Earth Mod");
    public static KeyBinding key_tp_place = new KeyBinding("Teleport to Place", Keyboard.KEY_P, "The Earth Mod");

    public static void init()
    {
        ClientRegistry.registerKeyBinding(key_streetview);
        ClientRegistry.registerKeyBinding(key_tp_place);
    }
}
