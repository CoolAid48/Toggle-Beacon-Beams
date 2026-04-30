package me.coolaid.tbb.network;

import me.coolaid.tbb.ToggleBeaconBeams;

public class ServerPresenceTracker {
    private static boolean serverHasMod = false;

    public static void setServerPresent(boolean present) {
        serverHasMod = present;
        ToggleBeaconBeams.LOGGER.info("Toggle Beacon Beam Mode Set To [{}] Mode", present ? "Server" : "Client-Only");
    }

    public static boolean isServerPresent() {
        return serverHasMod;
    }
}
