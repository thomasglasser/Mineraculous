package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import net.minecraft.tags.TagKey;

public class MiraculousTags {
    /// Miraculous that can use and store data for the {@link ButterflyCaneItem}
    public static final TagKey<Miraculous> CAN_USE_BUTTERFLY_CANE = create("can_use_butterfly_cane");
    /// Miraculous that can use and store data for the {@link CatStaffItem}
    public static final TagKey<Miraculous> CAN_USE_CAT_STAFF = create("can_use_cat_staff");
    /// Miraculous that can use and store data for the {@link LadybugYoyoItem}
    public static final TagKey<Miraculous> CAN_USE_LADYBUG_YOYO = create("can_use_ladybug_yoyo");
    public static final TagKey<Miraculous> CAN_USE_RABBIT_UMBRELLA = create("can_use_rabbit_umbrella");

    private static TagKey<Miraculous> create(String name) {
        return TagKey.create(MineraculousRegistries.MIRACULOUS, Mineraculous.modLoc(name));
    }
}
