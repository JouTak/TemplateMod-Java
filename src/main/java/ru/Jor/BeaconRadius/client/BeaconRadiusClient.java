package ru.Jor.BeaconRadius.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;


import ru.Jor.BeaconRadius.BeaconRadius;
public class BeaconRadiusClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        BeaconRadius.LOGGER.info("Client side of mod {} initialized!", BeaconRadius.MOD_ID);
        LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(RenderAndDraw::renderAndDraw);
    }







}
