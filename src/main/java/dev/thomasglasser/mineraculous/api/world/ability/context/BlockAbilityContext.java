package dev.thomasglasser.mineraculous.api.world.ability.context;

import net.minecraft.core.BlockPos;

public record BlockAbilityContext(BlockPos pos) implements AbilityContext {
    public static final String ADVANCEMENT_CONTEXT = "block";

    @Override
    public String advancementContext() {
        return ADVANCEMENT_CONTEXT;
    }
}
