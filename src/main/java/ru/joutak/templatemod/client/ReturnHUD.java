package ru.joutak.templatemod.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import ru.joutak.templatemod.TemplateMod;

import java.util.Locale;

public class ReturnHUD {
    private static final Identifier ARROW_TEXTURE = Identifier.fromNamespaceAndPath(
            TemplateMod.MOD_ID, "textures/waypoint.png"
    );

    public static void draw(GuiGraphicsExtractor graphics, float angleDegrees, double distance, int remainingPoints) {
        var matrices = graphics.pose();

        matrices.pushMatrix();
        matrices.translate(40f, graphics.guiHeight() - 56f);
        matrices.rotate((float) Math.toRadians(angleDegrees + 180f));

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ARROW_TEXTURE,
                -16, -16,
                0f, 0f,
                32, 32,
                32, 32,
                32, 32
        );

        matrices.popMatrix();

        graphics.text(
                Minecraft.getInstance().font,
                Component.translatable("hud.templatemod.return_distance", String.format(Locale.ROOT, "%.1f", distance)),
                8, graphics.guiHeight() - 36,
                0xFFFFFFFF
        );
        graphics.text(
                Minecraft.getInstance().font,
                Component.translatable("hud.templatemod.return_progress", remainingPoints),
                8, graphics.guiHeight() - 24,
                0xFFFFFFFF
        );
    }
}
