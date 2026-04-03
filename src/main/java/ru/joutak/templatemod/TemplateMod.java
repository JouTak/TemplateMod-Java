package ru.joutak.templatemod;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMod implements ModInitializer {

    public static final String MOD_ID = "templatemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static TemplateMod instance;

    public static TemplateMod getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Mod {} initialized!", MOD_ID);
    }
}
