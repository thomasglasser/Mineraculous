package dev.thomasglasser.mineraculous.api.world.ability;

/**
 * Holds relevant {@link Ability} information.
 *
 * @param powerLevel  The power level of the performer
 * @param powerActive Whether the performer's power is active
 */
public record AbilityData(int powerLevel, boolean powerActive) {}
