package me.coolaid.tbb.neoforge;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.config.ConfigScreen;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(ToggleBeaconBeams.MOD_ID)
public final class ToggleBeaconBeamsNeoForge {
    public ToggleBeaconBeamsNeoForge() {
        // Run our common setup.
        ToggleBeaconBeams.init();

        // Register the config screen with NeoForge's built-in mod menu
        ModList.get().getModContainerById("toggle-beacon-beams").ifPresent(mod -> {
            mod.registerExtensionPoint(IConfigScreenFactory.class,
                    (IConfigScreenFactory)(minecraft, parent) -> new ConfigScreen(parent));
        });
    }
}