package ru.joutak.templatemod.client;

import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RouteNavigator {
    private static final double REACH_RADIUS = 1.5;

    private List<Vec3> points = List.of();
    private int targetIndex = -1;

    public boolean start(List<Vec3> route) {
        this.points = List.copyOf(route);
        this.targetIndex = this.points.size() - 1;
        return isActive();
    }

    public boolean tick(Vec3 position) {
        if (!isActive()) {
            return false;
        }

        while (isActive()
                && position.distanceToSqr(this.points.get(this.targetIndex)) <= REACH_RADIUS * REACH_RADIUS) {
            this.targetIndex--;
        }

        // Сообщаем о завершении только в тот тик, когда достигнута первая точка.
        return !isActive();
    }

    public void stop() {
        this.points = List.of();
        this.targetIndex = -1;
    }

    public boolean isActive() {
        return this.targetIndex >= 0;
    }

    public Vec3 getTarget() {
        return isActive() ? this.points.get(this.targetIndex) : null;
    }

    public int getRemainingPointCount() {
        return this.targetIndex + 1;
    }
}
