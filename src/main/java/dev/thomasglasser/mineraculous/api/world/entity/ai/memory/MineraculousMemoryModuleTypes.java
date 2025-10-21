package dev.thomasglasser.mineraculous.api.world.entity.ai.memory;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MineraculousMemoryModuleTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<DuplicationStatus>> DUPLICATION_STATUS = MEMORY_MODULE_TYPES.register("duplication_status", () -> new MemoryModuleType<>(Optional.of(DuplicationStatus.CODEC)));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> DUPLICATES_MADE = MEMORY_MODULE_TYPES.register("duplicates_made", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

    public static void init() {}
}
