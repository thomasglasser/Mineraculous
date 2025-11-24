package dev.thomasglasser.mineraculous.api.world.ability;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import java.util.List;
import net.minecraft.nbt.CompoundTag;

/**
 * Holds relevant {@link Ability} information.
 *
 * @param powerLevel  The power level of the performer
 * @param powerActive Whether the performer's power is active
 */
public record AbilityData(int powerLevel, boolean powerActive, List<CompoundTag> storedEntities) {
    public static AbilityData of(MiraculousData data) {
        return new AbilityData(data.powerLevel(), data.powerActive(), data.storedEntities());
    }

    public static AbilityData of(KamikotizationData data) {
        return new AbilityData(data.kamikoData().powerLevel(), data.powerActive(), ImmutableList.of());
    }
}
