package me.coolaid.tbb.fabric;

import me.coolaid.tbb.ToggleBeaconBeams;
import net.fabricmc.api.ClientModInitializer;

public class ToggleBeaconBeamsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Run common setup.
        ToggleBeaconBeams.init();
    }
}