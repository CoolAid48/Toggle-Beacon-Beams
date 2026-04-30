package me.coolaid.tbb.fabric;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.ToggleBeaconBeamsClient;
import me.coolaid.tbb.network.ServerPresenceTracker;
import me.coolaid.tbb.network.TbbNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ToggleBeaconBeamsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Run common setup.
        ToggleBeaconBeams.init();
        ToggleBeaconBeamsClient.init();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            boolean present = ClientPlayNetworking.canSend(TbbNetworkHandler.ServerAckRequestPayload.TYPE)
                || ClientPlayNetworking.canSend(TbbNetworkHandler.ServerAckResponsePayload.TYPE);
            ServerPresenceTracker.setServerPresent(present);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, sender) -> {
            ServerPresenceTracker.setServerPresent(false);
        });
    }
}