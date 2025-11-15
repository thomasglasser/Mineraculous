package dev.thomasglasser.mineraculous.api.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.MineraculousMemoryModuleTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

public class Replicate<E extends Mob> extends ExtendedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).usesMemories(MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), MineraculousMemoryModuleTypes.REPLICATES_MADE.get());

    private BiConsumer<E, E> onReplication = (duplicate, original) -> {};

    public Replicate<E> onReplication(BiConsumer<E, E> onReplication) {
        this.onReplication = onReplication;

        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        int replicatesMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.REPLICATES_MADE.get(), () -> 0);
        return super.checkExtraStartConditions(level, entity) && replicatesMade < MineraculousServerConfig.get().maxKamikoReplicates.getAsInt();
    }

    @Override
    protected void start(E entity) {
        super.start(entity);
        Integer replicatesMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.REPLICATES_MADE.get(), () -> 0);
        if (replicatesMade < MineraculousServerConfig.get().maxKamikoReplicates.getAsInt()) {
            replicate(entity);
            BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.REPLICATES_MADE.get(), replicatesMade + 1);
        }
    }

    protected void replicate(E entity) {
        E replicate = (E) entity.getType().create(entity.level());
        if (replicate != null) {
            replicate.copyPosition(entity);
            onReplication.accept(replicate, entity);
            entity.level().addFreshEntity(replicate);
        } else {
            MineraculousConstants.LOGGER.error("Failed to replicate entity {}", entity);
        }
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        int replicatesMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.REPLICATES_MADE.get(), () -> 0);
        if (replicatesMade >= MineraculousServerConfig.get().maxKamikoReplicates.getAsInt()) {
            BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.HAS_REPLICATED.get(), Unit.INSTANCE);
            BrainUtils.clearMemories(entity, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), MineraculousMemoryModuleTypes.REPLICATES_MADE.get());
        }
    }
}
