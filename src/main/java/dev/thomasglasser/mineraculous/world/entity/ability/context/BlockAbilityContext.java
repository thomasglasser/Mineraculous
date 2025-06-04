package dev.thomasglasser.mineraculous.world.entity.ability.context;

import net.minecraft.core.BlockPos;

public record BlockAbilityContext(BlockPos pos) implements AbilityContext {}
