package me.coolaid.tbb.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LocalToggleStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path STORE_PATH = Path.of("config", "tbb_local_toggles.json");
    private static Map<String, Boolean> toggles = new HashMap<>();

    private static String getKeyFrom(String worldIdentifier, ResourceKey<Level> dimension, BlockPos pos) {
        return worldIdentifier + "@" + dimension.identifier() + "@" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static boolean isHidden(String worldIdentifier, ResourceKey<Level> dimension, BlockPos pos) {
        return toggles.getOrDefault(getKeyFrom(worldIdentifier, dimension, pos), false);
    }

    public static void setHidden(String worldIdentifier, ResourceKey<Level> dimension, BlockPos pos, boolean hidden) {
        if (setHiddenInMemory(worldIdentifier, dimension, pos, hidden)) {
            save();
        }
    }

    public static boolean setHiddenInMemory(String worldIdentifier, ResourceKey<Level> dimension, BlockPos pos, boolean hidden) {
        String key = getKeyFrom(worldIdentifier, dimension, pos);
        if (hidden) {
            if (Boolean.TRUE.equals(toggles.get(key))) return false;
            toggles.put(key, true);
            return true;
        }

        return toggles.remove(key) != null;
    }

    public static void load() {
        if (Files.exists(STORE_PATH)) {
            try {
                Type type = new TypeToken<Map<String, Boolean>>() {}.getType();
                toggles = GSON.fromJson(Files.readString(STORE_PATH), type);
                if (toggles == null) toggles = new HashMap<>();
            } catch (Exception _) {
                toggles = new HashMap<>();
            }
        }
    }

    public static void save() {
        try {
            Files.createDirectories(STORE_PATH.getParent());
            Files.writeString(STORE_PATH, GSON.toJson(toggles));
        } catch (Exception _) {}
    }

    public static void clear() {
        toggles.clear();
        save();
    }
}
