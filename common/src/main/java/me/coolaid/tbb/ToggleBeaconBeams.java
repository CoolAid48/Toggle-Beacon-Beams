package me.coolaid.tbb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ToggleBeaconBeams {
    public static final String MOD_ID = "toggle-beacon-beams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final int TOGGLE_BEAM_BUTTON_ID = 1337;

    private static boolean hideAllBeams = true;

    public static void init() {
        LOGGER.info("Let there be light!");
    }

    public static boolean isHideAllBeamsEnabled() {
        return hideAllBeams;
    }

    public static void toggleHideAllBeams() {
        setHideAllBeams(!hideAllBeams);
    }

    public static void setHideAllBeams(boolean enabled) {
        // Toggles all Beacon Beams and logs it
        hideAllBeams = enabled;
        LOGGER.info("Global beacon beam hiding: {}", enabled ? "enabled" : "disabled");
    }
}