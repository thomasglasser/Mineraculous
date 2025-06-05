package dev.thomasglasser.mineraculous.world.entity.ability.context;

import net.minecraft.core.BlockPos;

public record BlockAbilityContext(BlockPos pos) implements AbilityContext {
    public static final String ADVANCEMENT_CONTEXT = "block";

    @Override
    public String advancementContext() {
        return ADVANCEMENT_CONTEXT;
    }
}
