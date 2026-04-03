package ru.joutak.templatemod.client;

import net.fabricmc.api.ClientModInitializer;
import ru.joutak.templatemod.TemplateMod;

public class TemplateModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TemplateMod.LOGGER.info("Client side of mod {} initialized!", TemplateMod.MOD_ID);
    }
}
