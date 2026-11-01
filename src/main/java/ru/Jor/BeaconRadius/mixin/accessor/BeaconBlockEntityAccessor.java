package ru.Jor.BeaconRadius.mixin.accessor;

import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {
    @Accessor("levels")
    int BeaconRadius$getLevels();

    @Accessor("beamSections")
    List<BeaconBeamOwner.Section> BeaconRadius$getbeamSections();
}
