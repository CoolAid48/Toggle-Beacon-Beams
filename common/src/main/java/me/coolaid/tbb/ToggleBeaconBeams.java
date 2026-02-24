package me.coolaid.tbb;

import me.coolaid.tbb.config.ConfigManager;
import me.coolaid.tbb.util.BeamToggleAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class ToggleBeaconBeams {
    public static final String MOD_ID = "toggle-beacon-beams";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int TOGGLE_BEAM_BUTTON_ID = 1337;
    public static final int HIDE_BEAM_BUTTON_ID = 1338;
    public static final int SHOW_BEAM_BUTTON_ID = 1339;

    public static void init() {
        ConfigManager.load();

        LOGGER.info("Let there be light!");
    }

    public static boolean canUseClientConfigScreen() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getCurrentServer() != null;
    }

    public static void setAllLoadedBeaconsHidden(boolean hide) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        List<BlockPos> updatedPositions = new ArrayList<>();

        for (BlockEntity be : mc.level.getGloballyRenderedBlockEntities()) {
            if (be instanceof BeaconBlockEntity beacon) {
                BeamToggleAccess access = (BeamToggleAccess) beacon;
                if (access.beamToggle$isHidden() != hide) {
                    access.beamToggle$setHidden(hide);
                    mc.level.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
                    updatedPositions.add(beacon.getBlockPos().immutable());
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
                            serverLevel.sendBlockUpdated(pos, serverBeacon.getBlockState(), serverBeacon.getBlockState(), 3);
                        }
                    }
                }
            });
        }
    }
}