package me.coolaid.tbb.fabric;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.network.TbbNetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ToggleBeaconBeamsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ToggleBeaconBeams.init();

        PayloadTypeRegistry.serverboundPlay().register(
            TbbNetworkHandler.ServerAckRequestPayload.TYPE,
            TbbNetworkHandler.ServerAckRequestPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
            TbbNetworkHandler.ServerAckRequestPayload.TYPE,
            (payload, context) -> {
                // Intentionally left blank
            }
        );
    }
}
