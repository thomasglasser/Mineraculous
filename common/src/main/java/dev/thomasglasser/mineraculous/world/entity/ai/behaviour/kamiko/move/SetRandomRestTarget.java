package dev.thomasglasser.mineraculous.world.entity.ai.behaviour.kamiko.move;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.phys.Vec3;
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
    /*
    private int tries = 16;
    @Override
    protected Vec3 getTargetPos(E entity) {
        Vec3 entityFacing = entity.getViewVector(0);
        Vec3 pos = AirAndWaterRandomPos.getPos(entity, (int)(Math.ceil(this.radius.xzRadius())), (int)Math.ceil(this.radius.yRadius()), -2, entityFacing.x, entityFacing.z, Mth.HALF_PI);
        if (pos == null) return null;
        pos = new Vec3(pos.x(),entity.getY(),pos.z());
        return pos;
    }
    */
}
