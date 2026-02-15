package me.coolaid.tbb.fabric;

import me.coolaid.tbb.ToggleBeaconBeams;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ToggleBeaconBeamsFabricClient implements ClientModInitializer {

    private static KeyMapping toggleBeamsKey;
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.parse("tbb"));

    @Override
    public void onInitializeClient() {
        toggleBeamsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.tbb.toggle_all",
                GLFW.GLFW_KEY_B,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;

            while (toggleBeamsKey.consumeClick()) {
                ToggleBeaconBeams.toggleAllLoadedBeacons();
            }
        });

        // Run our common setup.
        ToggleBeaconBeams.init();
    }
}