package dev.thomasglasser.mineraculous.api.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.DuplicationStatus;
import dev.thomasglasser.mineraculous.api.world.entity.ai.memory.MineraculousMemoryModuleTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;
import net.tslat.smartbrainlib.util.BrainUtils;

public class DuplicateAndCopyMemories<E extends Mob> extends ExtendedBehaviour<E> {
    private static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2).usesMemories(MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get(), MineraculousMemoryModuleTypes.DUPLICATES_MADE.get());

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
        if (entity.tickCount % 10 == 0) {
            Integer duplicatesMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), () -> 0);
            System.out.println("Duplication number " + duplicatesMade + "!");
            BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), duplicatesMade + 1);
        }
    }

    @Override
    protected void stop(E entity) {
        super.stop(entity);
        int duplicatesMade = BrainUtils.memoryOrDefault(entity, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get(), () -> 0);
        if (duplicatesMade >= MineraculousServerConfig.get().maxKamikoDuplicates.getAsInt()) {
            BrainUtils.setMemory(entity, MineraculousMemoryModuleTypes.DUPLICATION_STATUS.get(), DuplicationStatus.HAS_DUPLICATED);
            BrainUtils.clearMemory(entity, MineraculousMemoryModuleTypes.DUPLICATES_MADE.get());
        }
    }
}
