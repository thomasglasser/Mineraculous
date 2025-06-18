package dev.thomasglasser.mineraculous.api.world.ability.context;

import dev.thomasglasser.mineraculous.api.world.ability.Ability;

/// Can be passed to an {@link Ability} for specialized behavior.
public interface AbilityContext {
    /// Passed to an advancement trigger when consumed.
    String advancementContext();
}
