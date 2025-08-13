package dev.thomasglasser.mineraculous.api.world.ability.context;

/// Can be passed to an {@link Ability} for specialized behavior.
public interface AbilityContext {
    /// Passed to an advancement trigger when consumed.
    String advancementContext();
}
