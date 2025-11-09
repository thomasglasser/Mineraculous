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

public class Duplicate<E extends Mob> extends ExtendedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).usesMemories(MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get(), MineraculousMemoryModuleTypes.DUPLICATES_MADE.get());

    private BiConsumer<E, E> onDuplication = (duplicate, original) -> {};

    public Duplicate<E> onDuplication(BiConsumer<E, E> onDuplication) {
        this.onDuplication = onDuplication;

        return this;
    }

    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
        int duplicatesMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), () -> 0);
        return super.checkExtraStartConditions(level, entity) && duplicatesMade < MineraculousServerConfig.get().maxKamikoDuplicates.getAsInt();
    }

    @Override
    protected void start(E entity) {
        super.start(entity);
        Integer duplicatesMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), () -> 0);
        if (entity.tickCount % 10 == 0 && duplicatesMade < MineraculousServerConfig.get().maxKamikoDuplicates.getAsInt()) {
            duplicate(entity);
            BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), duplicatesMade + 1);
        }
    }

    protected void duplicate(E entity) {
        E duplicate = (E) entity.getType().create(entity.level());
        if (duplicate != null) {
            duplicate.copyPosition(entity);
            onDuplication.accept(duplicate, entity);
            entity.level().addFreshEntity(duplicate);
        } else {
            MineraculousConstants.LOGGER.error("Failed to duplicate entity {}", entity);
        }
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        int duplicatesMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), () -> 0);
        if (duplicatesMade >= MineraculousServerConfig.get().maxKamikoDuplicates.getAsInt()) {
            BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.HAS_DUPLICATED.get(), Unit.INSTANCE);
            BrainUtils.clearMemories(entity, MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get(), MineraculousMemoryModuleTypes.DUPLICATES_MADE.get());
        }
    }
}
