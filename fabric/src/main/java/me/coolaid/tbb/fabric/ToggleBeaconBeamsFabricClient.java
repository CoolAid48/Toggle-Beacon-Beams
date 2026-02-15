package me.coolaid.tbb.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.tbb.ToggleBeaconBeams;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ToggleBeaconBeamsFabricClient implements ClientModInitializer {

    private static final String TOGGLE_ALL_BEAMS_KEY = "key.tbb.toggle_all";
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.parse("toggle-beacon-beams"));

    @Override
    public void onInitializeClient() {
        KeyMapping toggleAllBeamsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                TOGGLE_ALL_BEAMS_KEY,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleAllBeamsKey.consumeClick()) {
                ToggleBeaconBeams.toggleHideAllBeams();
            }
        });

        // Run our common setup.
        ToggleBeaconBeams.init();
    }
}