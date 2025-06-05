package dev.thomasglasser.mineraculous.world.entity.miraculous;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.ability.Abilities;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;

public class Miraculouses {
    public static final ResourceKey<Miraculous> BUTTERFLY = create("butterfly");
    public static final ResourceKey<Miraculous> CAT = create("cat");
    public static final ResourceKey<Miraculous> LADYBUG = create("ladybug");

    private static ResourceKey<Miraculous> create(String name) {
        return ResourceKey.create(MineraculousRegistries.MIRACULOUS, Mineraculous.modLoc(name));
    }

    public static void bootstrap(BootstrapContext<Miraculous> context) {
        HolderGetter<Ability> abilities = context.lookup(MineraculousRegistries.ABILITY);

        context.register(BUTTERFLY, new Miraculous(
                TextColor.fromRgb(0x641d9a),
                MineraculousCuriosProvider.SLOT_BROOCH,
                7,
                Optional.of(MineraculousItems.BUTTERFLY_CANE.toStack()),
                Optional.empty(),
                Optional.of(abilities.getOrThrow(Abilities.KAMIKOTIZATION)),
                List.of(
                        abilities.getOrThrow(Abilities.KAMIKO_CONTROL),
                        abilities.getOrThrow(Abilities.KAMIKOTIZED_COMMUNICATION)),
                MineraculousSoundEvents.BUTTERFLY_TRANSFORM,
                MineraculousSoundEvents.GENERIC_DETRANSFORM,
                MineraculousSoundEvents.GENERIC_TIMER_BEEP,
                MineraculousSoundEvents.GENERIC_TIMER_END));
        context.register(CAT, new Miraculous(
                TextColor.fromRgb(0xc6f800),
                MineraculousCuriosProvider.SLOT_RING,
                9,
                Optional.of(MineraculousItems.CAT_STAFF.toStack()),
                Optional.of("belt"),
                Optional.of(abilities.getOrThrow(Abilities.CATACLYSM)),
                List.of(
                        abilities.getOrThrow(Abilities.CAT_VISION),
                        abilities.getOrThrow(Abilities.PASSIVE_UNLUCK)),
                MineraculousSoundEvents.CAT_TRANSFORM,
                MineraculousSoundEvents.GENERIC_DETRANSFORM,
                MineraculousSoundEvents.GENERIC_TIMER_BEEP,
                MineraculousSoundEvents.GENERIC_TIMER_END));
        context.register(LADYBUG, new Miraculous(
                TextColor.fromRgb(0xdd1731),
                MineraculousCuriosProvider.SLOT_EARRINGS,
                9,
                Optional.of(MineraculousItems.LADYBUG_YOYO.toStack()),
                Optional.of("belt"),
                Optional.of(abilities.getOrThrow(Abilities.LUCKY_CHARM)),
                List.of(
                        abilities.getOrThrow(Abilities.MIRACULOUS_LADYBUG),
                        abilities.getOrThrow(Abilities.PASSIVE_LUCK)),
                MineraculousSoundEvents.LADYBUG_TRANSFORM,
                MineraculousSoundEvents.GENERIC_DETRANSFORM,
                MineraculousSoundEvents.GENERIC_TIMER_BEEP,
                MineraculousSoundEvents.GENERIC_TIMER_END));
    }
}
