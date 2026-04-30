package me.coolaid.tbb.neoforge.network;

import me.coolaid.tbb.ToggleBeaconBeams;
import me.coolaid.tbb.network.TbbNetworkHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkInit {
    @SubscribeEvent
    private static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ToggleBeaconBeams.MOD_ID);

//        registrar.playToServer(
//                TbbNetworkHandler.ServerAckRequestPayload.TYPE,
//                TbbNetworkHandler.ServerAckRequestPayload.CODEC,
//                (payload, context) -> {
//                    // server receives packet
//                }
//        );

        registrar.optional().playToClient(
                TbbNetworkHandler.ServerAckResponsePayload.TYPE,
                TbbNetworkHandler.ServerAckResponsePayload.CODEC,
                (payload, context) -> {
                    // client receives packet
                    // Intentionally left blank
                }
        );
    }
}
