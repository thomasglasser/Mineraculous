package dev.thomasglasser.mineraculous.api.world.entity.ai.memory;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class MineraculousMemoryModuleTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<DuplicationState>> DUPLICATION_STATUS = register("duplication_status");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> HAS_DUPLICATED = register("has_duplicated");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> DUPLICATES_MADE = register("duplicates_made");

    private static <T> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<T>> register(String name) {
        return MEMORY_MODULE_TYPES.register(name, () -> new MemoryModuleType<>(Optional.empty()));
    }

    public static void init() {}
}
