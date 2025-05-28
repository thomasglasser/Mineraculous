package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import net.minecraft.tags.TagKey;

public class MiraculousTags {
    public static final TagKey<Miraculous> CAN_USE_BUTTERFLY_CANE = create("can_use_butterfly_cane");
    public static final TagKey<Miraculous> CAN_USE_CAT_STAFF = create("can_use_cat_staff");
    public static final TagKey<Miraculous> CAN_USE_LADYBUG_YOYO = create("can_use_ladybug_yoyo");

    private static TagKey<Miraculous> create(String name) {
        return TagKey.create(MineraculousRegistries.MIRACULOUS, Mineraculous.modLoc(name));
    }
}
