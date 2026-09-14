package ru.joutak.templatemod.compat;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import ru.joutak.templatemod.TemplateMod;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehaviorManager;
import net.minecraft.world.phys.Vec3;
import ru.joutak.templatemod.client.RoutLighting;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

public class DynamicLightsIntegration implements DynamicLightsInitializer {
    private final Map<Vec3, WaypointLight> activeLights = new HashMap<>();
    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        TemplateMod.LOGGER.info("Поддержка LambDynamicLights подключена");

        var lights = context.dynamicLightBehaviorManager();

        RoutLighting.setUpdater(points -> updateLights(points, lights));
    }
    private void updateLights(
            List<Vec3> points,
            DynamicLightBehaviorManager lights
    ) {
        var currentPoints = new HashSet<>(points);

        for (Vec3 oldPoint : List.copyOf(this.activeLights.keySet())) {
            if (!currentPoints.contains(oldPoint)) {
                WaypointLight light = this.activeLights.remove(oldPoint);
                lights.remove(light);
            }
        }

        for (Vec3 point : currentPoints) {
            if (!this.activeLights.containsKey(point)) {
                WaypointLight light = new WaypointLight(
                        point.add(0.0, 0.5, 0.0),
                        10
                );

                lights.add(light);
                this.activeLights.put(point, light);
            }
        }
    }
}
