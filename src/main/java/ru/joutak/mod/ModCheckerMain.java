package ru.joutak.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.joutak.mod.network.ModListPayload;

public class ModCheckerMain implements ModInitializer {

    public static final String MOD_ID = "mod_checker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ModCheckerMain instance;
    public static ModCheckerMain getInstance() {
        return instance;
    }

    public static final Identifier NETWORK_ID = Identifier.fromNamespaceAndPath("mod_checker", "main");    @Override
    public void onInitialize() {
        instance = this;
        PayloadTypeRegistry.playC2S().register(ModListPayload.TYPE, ModListPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModListPayload.TYPE, ModListPayload.CODEC);
        LOGGER.info("Mod {} initialized!", MOD_ID);
    }
}
