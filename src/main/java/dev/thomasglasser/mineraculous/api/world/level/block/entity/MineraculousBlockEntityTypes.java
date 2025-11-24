package dev.thomasglasser.mineraculous.api.world.level.block.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.world.level.block.entity.OvenBlockEntity;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MineraculousBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OvenBlockEntity>> OVEN = BLOCK_ENTITY_TYPES.register("oven", () -> BlockEntityType.Builder.of(OvenBlockEntity::new, MineraculousBlocks.OVEN.get()).build(null));

    public static void init() {}
}
