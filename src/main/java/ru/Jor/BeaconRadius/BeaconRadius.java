package ru.Jor.BeaconRadius;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.awt.*;

public class BeaconRadius implements ModInitializer {

    public static final String MOD_ID = "beaconradius";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static BeaconRadius instance;

    public static BeaconRadius getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        instance = this;
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

                        if (Minecraft.getInstance().options.keyShift.isDown()) {
                            if (world.getBlockState(hitResult.getBlockPos()).is(Blocks.BEACON)) {
                                if(world.isClientSide()) {
                                    BRStorage.Toggle(hitResult.getBlockPos(),world.dimension().identifier());
                                }
                                return InteractionResult.SUCCESS;
                            }
                        }
                    return InteractionResult.PASS;
                });

        ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((player, world)->{
            if(player.getCurrentServer()!=null||player.getSingleplayerServer()!=null){
               BRStorage.onJoin();
            }
        });
        LOGGER.info("Mod {} initialized!", MOD_ID);
    }

}
