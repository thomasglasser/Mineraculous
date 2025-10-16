package dev.thomasglasser.mineraculous.api.world.ability;

import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;

/**
 * Holds relevant {@link Ability} information.
 *
 * @param powerLevel  The power level of the performer
 * @param powerActive Whether the performer's power is active
 */
public record AbilityData(int powerLevel, boolean powerActive) {
    public static AbilityData of(MiraculousData data) {
        return new AbilityData(data.powerLevel(), data.powerActive());
    }

    public static AbilityData of(KamikotizationData data) {
        return new AbilityData(data.kamikoData().powerLevel(), data.powerActive());
    }
}
