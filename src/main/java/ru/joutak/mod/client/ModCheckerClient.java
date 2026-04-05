package ru.joutak.mod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import ru.joutak.mod.ModCheckerMain;
import ru.joutak.mod.ModData;
import ru.joutak.mod.network.ModListPayload;

import java.util.ArrayList;
import java.util.List;

public class ModCheckerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ModListPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    List<ModData> mods = new ArrayList<>();
                    FabricLoader.getInstance().getAllMods().forEach(m -> {
                        mods.add(new ModData(
                                m.getMetadata().getId(),
                                m.getMetadata().getVersion().getFriendlyString()
                        ));
                    });

                    context.responseSender().sendPacket(new ModListPayload(mods));
                    ModCheckerMain.LOGGER.info("Sent {} mods to server", mods.size());
                } catch (Exception e) {
                    ModCheckerMain.LOGGER.error("Failed to gather mods: ", e);
                    context.responseSender().sendPacket(new ModListPayload(new ArrayList<>()));
                }
            });
        });
    }
}