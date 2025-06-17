package dev.thomasglasser.mineraculous.api.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

public class SetWalkTargetToLikedPlayer<E extends Mob> extends SetWalkTargetToAttackTarget<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3).hasMemory(MemoryModuleType.LIKED_PLAYER).usesMemories(MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET);

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(E entity) {
        Brain<?> brain = entity.getBrain();
        UUID targetId = BrainUtils.getMemory(entity, MemoryModuleType.LIKED_PLAYER);
        if (targetId != null) {
            Player target = entity.level().getPlayerByUUID(targetId);

            if (target != null) {
                if (entity.getSensing().hasLineOfSight(target) && BehaviorUtils.isWithinAttackRange(entity, target, 1)) {
                    BrainUtils.clearMemory(brain, MemoryModuleType.WALK_TARGET);
                } else {
                    BrainUtils.setMemory(brain, MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
                    BrainUtils.setMemory(brain, MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(target, false), this.speedMod.apply(entity, target), this.closeEnoughWhen.applyAsInt(entity, target)));
                }
            }
        }
    }
}
