package ru.joutak.templatemod.client;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.function.Consumer;

public class RoutLighting {
    private static Consumer<List<Vec3>> updater = points -> {};

    public static void setUpdater(Consumer<List<Vec3>> newUpdater) {
        updater = newUpdater;
    }

    public static void update(List<Vec3> points) {
        updater.accept(points);
    }
}
