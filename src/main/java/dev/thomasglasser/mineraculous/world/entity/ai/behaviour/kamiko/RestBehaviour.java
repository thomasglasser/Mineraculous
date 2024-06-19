package dev.thomasglasser.mineraculous.world.entity.ai.behaviour.kamiko;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.LevelAccessor;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

import java.util.List;

public class RestBehaviour<E extends Kamiko> extends ExtendedBehaviour<E> {

    @Override
    protected void start(E entity) {
        entity.setResting(true);
    }

    // Access
    public boolean timedOut(long gameTime) {
        return super.timedOut(gameTime);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        return canRest(entity,level);
    }

    public static boolean canRest(Kamiko entity, LevelAccessor level) {
        return level.getBlockCollisions(entity, entity.getBoundingBox().move(0,-1.0/16.0,0)).iterator().hasNext();
    }

    @Override
    protected boolean shouldKeepRunning(E entity) {
        return entity.isResting() && canRest(entity,entity.level());
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return List.of();
    }

    @Override
    protected void stop(E entity) {
        entity.setResting(false);
    }
}
