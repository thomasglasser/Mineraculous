package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class MineraculousDamageTypeTags {
    public static final TagKey<DamageType> IS_CATACLYSM = create("is_cataclysm");

    private static TagKey<DamageType> create(String id) {
        return TagKey.create(Registries.DAMAGE_TYPE, Mineraculous.modLoc(id));
    }
}
