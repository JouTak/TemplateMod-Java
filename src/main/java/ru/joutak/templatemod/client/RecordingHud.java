package ru.joutak.templatemod.client;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import ru.joutak.templatemod.TemplateMod;
import ru.joutak.templatemod.client.RoutRecorder.RecordingState;
public class RecordingHud {
    private static final Identifier RECORDING_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    TemplateMod.MOD_ID,
                    "textures/recording.png"
            );
    private static final Identifier PAUSED_TEXTURE =
            Identifier.fromNamespaceAndPath(
                    TemplateMod.MOD_ID,
                    "textures/pause.png"
            );
    public static void draw(GuiGraphicsExtractor graphics, RecordingState state) {
        if (state == RecordingState.STOPPED) {
            return;
        }

        Identifier texture;

        if (state == RecordingState.RECORDING) {
            texture = RECORDING_TEXTURE;
        } else {
            texture = PAUSED_TEXTURE;
        }

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                8, graphics.guiHeight() - 24,
                0f, 0f,
                16, 16,
                32, 32,
                32, 32
        );
    }
}
