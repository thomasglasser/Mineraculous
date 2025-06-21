package dev.thomasglasser.mineraculous.api.world.miraculous;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
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
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;

public class Miraculouses {
    public static final ResourceKey<Miraculous> BUTTERFLY = create("butterfly");
    public static final ResourceKey<Miraculous> CAT = create("cat");
    public static final ResourceKey<Miraculous> LADYBUG = create("ladybug");

    private static ResourceKey<Miraculous> create(String name) {
        return ResourceKey.create(MineraculousRegistries.MIRACULOUS, Mineraculous.modLoc(name));
    }

    public static final MapCodec<BaseRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("base").forGetter(BaseRecipe::base)
    ).apply(instance, BaseRecipe::new));

    public record BaseRecipe(Ingredient base) {}

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
                Optional.of("belt"),
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
                Optional.of("belt"),
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
