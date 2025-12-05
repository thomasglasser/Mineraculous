package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class MineraculousDamageTypeTags {
    /// Damage types that hurt {@link dev.thomasglasser.mineraculous.impl.world.entity.Kamiko}s.
    public static final TagKey<DamageType> HURTS_KAMIKOS = create("hurts_kamikos");

    /// Damage types that can be considered cataclysm ({@link dev.thomasglasser.mineraculous.api.world.damagesource.MineraculousDamageTypes#CATACLYSM} by default).
    public static final TagKey<DamageType> IS_CATACLYSM = create("is_cataclysm");

    private static TagKey<DamageType> create(String id) {
        return TagKey.create(Registries.DAMAGE_TYPE, MineraculousConstants.modLoc(id));
    }
}
