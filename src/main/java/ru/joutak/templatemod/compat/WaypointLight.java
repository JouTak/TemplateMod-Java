package ru.joutak.templatemod.compat;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Range;

public class WaypointLight implements DynamicLightBehavior{
    private final Vec3 position;
    private final int luminance;
    private boolean changed = true;

    public WaypointLight(Vec3 position, int luminance) {
        if (luminance < 0 || luminance > 15) {
            throw new IllegalArgumentException("Яркость должна быть от 0 до 15");
        }

        this.position = position;
        this.luminance = luminance;
    }

    @Override
    public @Range(from = 0L, to = 15L) double lightAtPos(BlockPos pos, double falloffRatio) {
        double dx = pos.getX() + 0.5 - this.position.x;
        double dy = pos.getY() + 0.5 - this.position.y;
        double dz = pos.getZ() + 0.5 - this.position.z;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        return Math.max(this.luminance - distance * falloffRatio, 0.0);
    }

    @Override
    public BoundingBox getBoundingBox() {
        BlockPos block = BlockPos.containing(this.position);

        return new BoundingBox(
                block.getX(),
                block.getY(),
                block.getZ(),
                block.getX() + 1,
                block.getY() + 1,
                block.getZ() + 1
        );
    }

    @Override
    public boolean hasChanged() {
        boolean result = this.changed;
        this.changed = false;
        return result;
    }
}
