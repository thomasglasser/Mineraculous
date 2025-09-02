package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class MineraculousDamageTypeTags {
    /// Damage types that hurt {@link Kamiko}s.
    public static final TagKey<DamageType> HURTS_KAMIKOS = create("hurts_kamikos");

    /// Damage types that can be considered cataclysm.
    public static final TagKey<DamageType> IS_CATACLYSM = create("is_cataclysm");

    private static TagKey<DamageType> create(String id) {
        return TagKey.create(Registries.DAMAGE_TYPE, Mineraculous.modLoc(id));
    }
}
