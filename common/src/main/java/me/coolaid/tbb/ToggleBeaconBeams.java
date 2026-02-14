package me.coolaid.tbb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ToggleBeaconBeams {
    public static final String MOD_ID = "toggle-beacon-beams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

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
        hideAllBeams = enabled;
        LOGGER.info("Global beacon beam hiding: {}", enabled ? "enabled" : "disabled");
    }
}