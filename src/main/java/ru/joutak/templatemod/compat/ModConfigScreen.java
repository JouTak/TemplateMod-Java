package ru.joutak.templatemod.compat;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.joutak.templatemod.config.ConfigLoader;
import ru.joutak.templatemod.config.ModConfig;

public class ModConfigScreen {
    public static Screen create(Screen parent) {
        ModConfig config = ConfigLoader.load();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.templatemod.title"));

        var category = builder.getOrCreateCategory(
                Component.translatable("config.templatemod.category.recording")
        );

        category.addEntry(
                builder.entryBuilder()
                        .startIntField(
                                Component.translatable("config.templatemod.recording_interval_ticks"),
                                config.getRecordingIntervalTicks()
                        )
                        .setMin(1)
                        .setDefaultValue(
                                new ModConfig().getRecordingIntervalTicks()
                        )
                        .setSaveConsumer(config::setRecordingIntervalTicks)
                        .requireRestart()
                        .build()
        );

        builder.setSavingRunnable(() -> ConfigLoader.save(config));

        return builder.build();
    }
}
