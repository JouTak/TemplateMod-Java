package ru.joutak.templatemod.client;
import ru.joutak.templatemod.config.ConfigLoader;
import ru.joutak.templatemod.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import ru.joutak.templatemod.TemplateMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;
import ru.joutak.templatemod.client.RoutRecorder.RecordingState;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.multiplayer.ClientLevel;

public class TemplateModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ModConfig config = ConfigLoader.load();

        this.recorder = new RoutRecorder(
                config.getRecordingIntervalTicks()
        );
        TemplateMod.LOGGER.info("Клиентская часть мода {} запущена", TemplateMod.MOD_ID);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.routeLevel != client.level) {
                this.recorder.clear();
                this.navigator.stop();
                RoutLighting.update(this.recorder.getPoints());
                this.routeLevel = client.level;
            }
            if (client.player == null || client.isPaused()) {
                return;
            }
            if (!client.player.isAlive()) {
                recorder.clear();
                navigator.stop();
                RoutLighting.update(recorder.getPoints());
                return;
            }
            while (this.StartRecording.consumeClick()) {
                navigator.stop();
                if (recorder.getState() == RecordingState.STOPPED) {
                    client.player.sendSystemMessage(Component.translatable("message.templatemod.recording_started"));
                } else {
                    client.player.sendSystemMessage(Component.translatable("message.templatemod.recording_stopped"));
                    if (this.startReturn.isUnbound()) {
                        client.player.sendSystemMessage(Component.translatable("message.templatemod.bind_return_key"));
                    } else {
                        client.player.sendSystemMessage(Component.translatable(
                                "message.templatemod.return_hint",
                                this.startReturn.getTranslatedKeyMessage()
                        ));
                    }
                }
                recorder.toggleRecording(client.player.position());
            }
            while (this.ShowList.consumeClick()) {
                if (recorder.getPoints().isEmpty()){
                    client.player.sendSystemMessage(Component.translatable("message.templatemod.points_empty"));
                }
                else {
                    for (Vec3 pos : recorder.getPoints()){
                        client.player.sendSystemMessage(Component.literal(pos.toString()));
                    }
                }
            }
            while (this.pauseRecording.consumeClick()) {
                recorder.togglePause();
            }
            while (this.startReturn.consumeClick()) {
                if (navigator.isActive()) {
                    navigator.stop();
                    client.player.sendSystemMessage(Component.translatable("message.templatemod.return_stopped"));
                } else if (recorder.getState() != RecordingState.STOPPED) {
                    client.player.sendSystemMessage(Component.translatable("message.templatemod.finish_recording"));
                } else if (navigator.start(recorder.getPoints())) {
                    client.player.sendSystemMessage(Component.translatable("message.templatemod.return_started"));
                } else {
                    client.player.sendSystemMessage(Component.translatable("message.templatemod.no_route"));
                }
            }
            while (this.clearRoute.consumeClick()) {
                recorder.clear();
                navigator.stop();
                client.player.sendSystemMessage(Component.translatable("message.templatemod.route_cleared"));
            }
            recorder.tick(client.player.position());
            if (navigator.tick(client.player.position())) {
                client.player.sendSystemMessage(Component.translatable("message.templatemod.return_finished"));
            }
            RoutLighting.update(recorder.getPoints());
        });
        LevelRenderEvents.END_EXTRACTION.register(context -> {
            RouteMarkers.extract(recorder.getPoints());
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(RouteMarkers::draw);
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(TemplateMod.MOD_ID, "recording_hud"),
                (graphics, deltaTracker) ->
                        RecordingHud.draw(graphics, recorder.getState())
        );
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(TemplateMod.MOD_ID, "return_hud"),
                (graphics, deltaTracker) -> {
                    var client = Minecraft.getInstance();
                    if (client.player == null || !navigator.isActive()) {
                        return;
                    }
                    Vec3 target = navigator.getTarget();
                    Vec3 position = client.player.position();
                    double dx = target.x - position.x;
                    double dz = target.z - position.z;
                    float angleDegrees = 0f;
                    if (dx * dx + dz * dz > 1.0E-6) {
                        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                        angleDegrees = Mth.wrapDegrees(targetYaw - client.player.getYRot());
                    }

                    ReturnHUD.draw(graphics, angleDegrees, position.distanceTo(target), navigator.getRemainingPointCount());
                }
        );
    }
    private RoutRecorder recorder;
    private final RouteNavigator navigator = new RouteNavigator();
    private ClientLevel routeLevel;
    KeyMapping.Category Pathtrack = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(TemplateMod.MOD_ID, "controls")
    );
    KeyMapping StartRecording = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.PathtrackMod.start-recording",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_C,
                    this.Pathtrack
            ));
    KeyMapping pauseRecording = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.templatemod.pause_recording",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_V,
                    this.Pathtrack
            )
    );
    KeyMapping ShowList = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key.templatemod.show_list",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_P,
            this.Pathtrack
    ));
    KeyMapping clearRoute = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.templatemod.clear_route",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_DELETE,
                    this.Pathtrack
            )
    );
    KeyMapping startReturn = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.templatemod.start_return",
                    InputConstants.Type.KEYSYM,
                    InputConstants.KEY_R,
                    this.Pathtrack
            )
    );
}
