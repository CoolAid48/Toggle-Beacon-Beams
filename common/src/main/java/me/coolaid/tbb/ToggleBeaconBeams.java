package me.coolaid.tbb;

import me.coolaid.tbb.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ToggleBeaconBeams {
    public static final String MOD_ID = "togglebeaconbeams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int TOGGLE_BEAM_BUTTON_ID = 1337;
    public static final int HIDE_BEAM_BUTTON_ID = 1338;
    public static final int SHOW_BEAM_BUTTON_ID = 1339;

    public static void init() {
        ConfigManager.load();
        LOGGER.info("Server - Let there be light!");
    }
}