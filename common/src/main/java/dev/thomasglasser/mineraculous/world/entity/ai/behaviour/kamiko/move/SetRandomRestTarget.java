package dev.thomasglasser.mineraculous.world.entity.ai.behaviour.kamiko.move;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.util.BrainUtils;

public class SetRandomRestTarget<E extends PathfinderMob> extends SetRandomWalkTarget<E> {
    @Override
    protected void start(E entity) {
        super.start(entity);
        entity.getBrain().getMemory(MemoryModuleType.WALK_TARGET).ifPresent(target -> {
            BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(target.getTarget(), target.getSpeedModifier(), 1));
        });
    }
}
