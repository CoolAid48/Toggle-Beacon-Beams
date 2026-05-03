package me.coolaid.tbb;

import me.coolaid.tbb.config.LocalToggleStore;
import me.coolaid.tbb.network.ServerPresenceTracker;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ToggleBeaconBeamsClient {
    public static void init() {
        LocalToggleStore.load();
        ToggleBeaconBeams.LOGGER.info("Client - Let there be light!");
    }

    public static boolean canUseClientConfigScreen() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getCurrentServer() != null;
    }

    public static void setAllLoadedBeaconsHidden(boolean hide) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        List<BlockPos> updatedPositions = new ArrayList<>();

        // Client-only fallback
        if (!ServerPresenceTracker.isServerPresent()) {
            String worldIdentifier = ToggleBeaconBeamsClient.getWorldUniqueIdentifier(mc);
            var dimension = mc.level.dimension();
            boolean storeChanged = false;

            for (BlockEntity be : mc.level.getGloballyRenderedBlockEntities()) {
                if (be instanceof BeaconBlockEntity) {
                    BlockPos pos = be.getBlockPos();
                    storeChanged |= LocalToggleStore.setHiddenInMemory(worldIdentifier, dimension, pos, hide);
                    var state = be.getBlockState();
                    mc.level.sendBlockUpdated(pos, state, state, 3);
                }
            }

            if (storeChanged) {
                LocalToggleStore.save();
            }
            return;
        }

        for (BlockEntity be : mc.level.getGloballyRenderedBlockEntities()) {
            if (be instanceof BeaconBlockEntity beacon) {
                BeamToggleAccess access = (BeamToggleAccess) beacon;
                if (access.beamToggle$isHidden() != hide) {
                    BlockPos pos = be.getBlockPos();
                    access.beamToggle$setHidden(hide);
                    updatedPositions.add(pos.immutable());
                }
            }
        }

        MinecraftServer server = mc.getSingleplayerServer();
        if (server != null && !updatedPositions.isEmpty()) {
            List<BlockPos> serverPositions = List.copyOf(updatedPositions);
            var dimension = mc.level.dimension();
            server.execute(() -> {
                ServerLevel serverLevel = server.getLevel(dimension);
                if (serverLevel == null) return;

                for (BlockPos pos : serverPositions) {
                    BlockEntity serverBe = serverLevel.getBlockEntity(pos);
                    if (serverBe instanceof BeaconBlockEntity serverBeacon) {
                        BeamToggleAccess serverAccess = (BeamToggleAccess) serverBeacon;
                        if (serverAccess.beamToggle$isHidden() != hide) {
                            serverAccess.beamToggle$setHidden(hide);
                        }
                    }
                }
            });
        }
    }

    @Nullable
    public static String getWorldUniqueIdentifier(Minecraft mc) {
        String worldIdentifier = null;
        ServerData data = mc.getCurrentServer();

        if (data != null) {
            worldIdentifier = data.ip;
        } else {
            IntegratedServer server = mc.getSingleplayerServer();
            if (server != null) {
                worldIdentifier = server.getWorldData().getLevelName();
            }
        }

        return worldIdentifier;
    }
}
