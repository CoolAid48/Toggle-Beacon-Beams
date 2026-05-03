package me.coolaid.tbb.neoforge;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.ToggleBeaconBeamsClient;
import me.coolaid.tbb.config.ConfigScreen;
import me.coolaid.tbb.network.ServerPresenceTracker;
import me.coolaid.tbb.network.TbbNetworkHandler;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public class ToggleBeaconBeamsNeoForgeClient {
    public static void init() {
        ToggleBeaconBeamsClient.init();

        // Register the config screen with NeoForge's built-in mod menu
        ModList.get().getModContainerById(ToggleBeaconBeams.MOD_ID).ifPresent(mod -> {
            mod.registerExtensionPoint(IConfigScreenFactory.class,
                    (IConfigScreenFactory)(minecraft, parent) -> new ConfigScreen(parent));
        });

        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            boolean present = event.getPlayer().connection
                    .hasChannel(TbbNetworkHandler.ServerAckResponsePayload.TYPE);
            ServerPresenceTracker.setServerPresent(present);
        });

        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut _) -> {
            ServerPresenceTracker.setServerPresent(false);
        });
    }
}
