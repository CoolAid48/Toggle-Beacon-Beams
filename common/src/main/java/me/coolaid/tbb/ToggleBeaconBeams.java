package me.coolaid.tbb;

import me.coolaid.tbb.config.ConfigManager;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ToggleBeaconBeams {
    public static final String MOD_ID = "toggle-beacon-beams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int TOGGLE_BEAM_BUTTON_ID = 1337;

    public static void init() {
        ConfigManager.load();

        LOGGER.info("Let there be light!");
    }

    public static void setAllLoadedBeaconsHidden(boolean hide) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (BlockEntity be : mc.level.getGloballyRenderedBlockEntities()) {
            if (be instanceof BeaconBlockEntity beacon) {
                BeamToggleAccess access = (BeamToggleAccess) beacon;
                // Only update if it's actually changing
                if (access.beamToggle$isHidden() != hide) {
                    access.beamToggle$setHidden(hide);
                    // Force sync and re-render
                    mc.level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
                }
            }
        }
    }
}