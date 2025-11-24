package dev.thomasglasser.mineraculous.api.world.miraculous;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.ability.Abilities;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.data.curios.MineraculousCuriosProvider;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

public class Miraculouses {
    /// Purple, brooch-powered, with {@link Abilities#KAMIKOTIZATION}.
    public static final ResourceKey<Miraculous> BUTTERFLY = create("butterfly");
    /// Green, ring-powered, with {@link Abilities#CATACLYSM}.
    public static final ResourceKey<Miraculous> CAT = create("cat");
    /// Red, earring-powered, with {@link Abilities#LUCKY_CHARM}.
    public static final ResourceKey<Miraculous> LADYBUG = create("ladybug");

    private static ResourceKey<Miraculous> create(String name) {
        return ResourceKey.create(MineraculousRegistries.MIRACULOUS, MineraculousConstants.modLoc(name));
    }

    @ApiStatus.Internal
    public static void bootstrap(BootstrapContext<Miraculous> context) {
        HolderGetter<Ability> abilities = context.lookup(MineraculousRegistries.ABILITY);

        context.register(BUTTERFLY, new Miraculous(
                TextColor.fromRgb(0x641d9a),
                MineraculousCuriosProvider.SLOT_BROOCH,
                Optional.of(7),
                MineraculousItems.BUTTERFLY_CANE.toStack(),
                Optional.empty(),
                abilities.getOrThrow(Abilities.KAMIKOTIZATION),
                HolderSet.direct(
                        abilities.getOrThrow(Abilities.KAMIKO_CONTROL),
                        abilities.getOrThrow(Abilities.KAMIKOTIZED_COMMUNICATION)),
                MineraculousSoundEvents.BUTTERFLY_TRANSFORM,
                MineraculousSoundEvents.GENERIC_DETRANSFORM,
                MineraculousSoundEvents.GENERIC_TIMER_WARNING,
                MineraculousSoundEvents.GENERIC_TIMER_END));
        context.register(CAT, new Miraculous(
                TextColor.fromRgb(0xc6f800),
                MineraculousCuriosProvider.SLOT_RING,
                Optional.of(9),
                MineraculousItems.CAT_STAFF.toStack(),
                Optional.of(MineraculousCuriosProvider.SLOT_BELT),
                abilities.getOrThrow(Abilities.CATACLYSM),
                HolderSet.direct(
                        abilities.getOrThrow(Abilities.CAT_VISION),
                        abilities.getOrThrow(Abilities.PASSIVE_UNLUCK)),
                MineraculousSoundEvents.CAT_TRANSFORM,
                MineraculousSoundEvents.GENERIC_DETRANSFORM,
                MineraculousSoundEvents.GENERIC_TIMER_WARNING,
                MineraculousSoundEvents.GENERIC_TIMER_END));
        context.register(LADYBUG, new Miraculous(
                TextColor.fromRgb(0xdd1731),
                MineraculousCuriosProvider.SLOT_EARRINGS,
                Optional.of(9),
                MineraculousItems.LADYBUG_YOYO.toStack(),
                Optional.of(MineraculousCuriosProvider.SLOT_BELT),
                abilities.getOrThrow(Abilities.LUCKY_CHARM),
                HolderSet.direct(
                        abilities.getOrThrow(Abilities.MIRACULOUS_LADYBUG),
                        abilities.getOrThrow(Abilities.PASSIVE_LUCK)),
                MineraculousSoundEvents.LADYBUG_TRANSFORM,
                MineraculousSoundEvents.GENERIC_DETRANSFORM,
                MineraculousSoundEvents.GENERIC_TIMER_WARNING,
                MineraculousSoundEvents.GENERIC_TIMER_END));
    }
}
