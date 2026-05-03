package me.coolaid.tbb.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class TbbNetworkHandler {

    public record ServerAckRequestPayload() implements CustomPacketPayload {
        public static final Identifier ACK_REQUEST_PAYLOAD_ID =
                Identifier.fromNamespaceAndPath("tbb", "server_ack_request");

        public static final CustomPacketPayload.Type<ServerAckRequestPayload> TYPE =
                new CustomPacketPayload.Type<>(ACK_REQUEST_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerAckRequestPayload> CODEC =
                StreamCodec.unit(new ServerAckRequestPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ServerAckResponsePayload() implements CustomPacketPayload {
        public static final Identifier ACK_RESPONSE_PAYLOAD_ID =
                Identifier.fromNamespaceAndPath("tbb", "server_ack_response");

        public static final CustomPacketPayload.Type<ServerAckResponsePayload> TYPE =
                new CustomPacketPayload.Type<>(ACK_RESPONSE_PAYLOAD_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerAckResponsePayload> CODEC =
                StreamCodec.unit(new ServerAckResponsePayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
