package me.coolaid.tbb.neoforge;

import me.coolaid.tbb.ToggleBeaconBeams;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(ToggleBeaconBeams.MOD_ID)
public final class ToggleBeaconBeamsNeoForge {
    public ToggleBeaconBeamsNeoForge() {
        // Run our common setup.
        ToggleBeaconBeams.init();

        if (FMLEnvironment.getDist().isClient()) {
            ToggleBeaconBeamsNeoForgeClient.init();
        }
    }
}