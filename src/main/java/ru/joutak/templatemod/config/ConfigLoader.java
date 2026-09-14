package ru.joutak.templatemod.config;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import ru.joutak.templatemod.TemplateMod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
public class ConfigLoader {
    private static final Gson GSON = new Gson();

    public static ModConfig load() {
        Path path = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("PathTrackMod-config.json");

        ModConfig defaults = new ModConfig();

        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GSON.toJson(defaults));
                return defaults;
            }

            String json = Files.readString(path);
            ModConfig config = GSON.fromJson(json, ModConfig.class);

            if (config == null || config.getRecordingIntervalTicks() < 1) {
                TemplateMod.LOGGER.warn(
                        "В конфиге {} неверный интервал записи. Использую {} тиков.",
                        path, defaults.getRecordingIntervalTicks()
                );
                return defaults;
            }

            return config;
        } catch (IOException | JsonParseException e) {
            TemplateMod.LOGGER.warn(
                    "Не удалось прочитать конфиг {}. Использую стандартные настройки.", path, e
            );
            return defaults;
        }
    }

    public static void save(ModConfig config) {
        Path path = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("PathTrackMod-config.json");

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config));
        } catch (IOException e) {
            TemplateMod.LOGGER.error("Не удалось сохранить конфиг {}.", path, e);
        }

    }
}
