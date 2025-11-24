package dev.thomasglasser.mineraculous.api.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.MineraculousMemoryModuleTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

public class Replicate<E extends Mob> extends ExtendedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(3).usesMemories(MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), MineraculousMemoryModuleTypes.REPLICAS_MADE.get(), MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get());

    private BiConsumer<E, E> onReplication = (replica, original) -> {};

    public Replicate<E> onReplication(BiConsumer<E, E> onReplication) {
        this.onReplication = onReplication;

        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected void start(E entity) {
        super.start(entity);
        int waitTicks = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get(), () -> 0);
        int replicasMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.REPLICAS_MADE.get(), () -> 0);
        if (waitTicks > 0) {
            waitTicks--;
            if (waitTicks <= 0) {
                BrainUtils.clearMemory(entity, MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get());
            } else {
                BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get(), waitTicks);
            }
        } else if (replicasMade < MineraculousServerConfig.get().maxKamikoReplicas.getAsInt()) {
            replicate(entity, replicasMade);
        } else {
            stop(entity);
        }
    }

    protected void replicate(E entity, int replicasMade) {
        E replica = (E) entity.getType().create(entity.level());
        if (replica != null) {
            replica.copyPosition(entity);
            onReplication.accept(replica, entity);
            entity.level().addFreshEntity(replica);
            BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.REPLICAS_MADE.get(), replicasMade + 1);
        } else {
            MineraculousConstants.LOGGER.error("Failed to replicate entity {}", entity);
        }
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        int replicasMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.REPLICAS_MADE.get(), () -> 0);
        if (replicasMade >= MineraculousServerConfig.get().maxKamikoReplicas.getAsInt()) {
            BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.HAS_REPLICATED.get(), Unit.INSTANCE);
            BrainUtils.clearMemories(entity, MineraculousMemoryModuleTypes.REPLICATION_STATUS.get(), MineraculousMemoryModuleTypes.REPLICAS_MADE.get(), MineraculousMemoryModuleTypes.REPLICATION_WAIT_TICKS.get());
        }
    }
}
