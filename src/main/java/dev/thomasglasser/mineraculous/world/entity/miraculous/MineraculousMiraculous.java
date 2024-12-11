package dev.thomasglasser.mineraculous.world.entity.miraculous;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.data.curios.MineraculousCuriosProvider;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.MineraculousAbilities;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public class MineraculousMiraculous {
    public static final ResourceKey<Miraculous> BUTTERFLY = register("butterfly");
    public static final ResourceKey<Miraculous> CAT = register("cat");
    public static final ResourceKey<Miraculous> LADYBUG = register("ladybug");

    private static ResourceKey<Miraculous> register(String id) {
        return ResourceKey.create(MineraculousRegistries.MIRACULOUS, Mineraculous.modLoc(id));
    }

    public static void bootstrap(BootstrapContext<Miraculous> context) {
        HolderGetter<Ability> abilities = context.lookup(MineraculousRegistries.ABILITY);

        context.register(BUTTERFLY, new Miraculous(
                TextColor.fromRgb(0x641d9a),
                MineraculousCuriosProvider.SLOT_BROOCH,
                MineraculousItems.BUTTERFLY_CANE.get().getDefaultInstance(),
                // TODO: Add kwami hungry sound
                Optional.empty(),
                Optional.of(abilities.getOrThrow(MineraculousAbilities.KAMIKOTIZATION)),
                List.of(
                        abilities.getOrThrow(MineraculousAbilities.KAMIKO_CONTROL),
                        abilities.getOrThrow(MineraculousAbilities.KAMIKOTIZED_COMMUNICATION)),
                MineraculousSoundEvents.BUTTERFLY_TRANSFORM));
        context.register(CAT, new Miraculous(
                TextColor.fromRgb(0xc6f800),
                MineraculousCuriosProvider.SLOT_RING,
                MineraculousItems.CAT_STAFF.get().getDefaultInstance(),
                // TODO: Add kwami hungry sound
                Optional.empty(),
                Optional.of(abilities.getOrThrow(MineraculousAbilities.CATACLYSM)),
                List.of(abilities.getOrThrow(MineraculousAbilities.CAT_VISION)),
                MineraculousSoundEvents.CAT_TRANSFORM));
        context.register(LADYBUG, new Miraculous(
                TextColor.fromRgb(0xdd1731),
                MineraculousCuriosProvider.SLOT_EARRINGS,
                MineraculousItems.LADYBUG_YOYO.get().getDefaultInstance(),
                // TODO: Add kwami hungry sound
                Optional.empty(),
                Optional.empty(/*abilities.getOrThrow(MineraculousAbilities.MIRACULOUS_LADYBUG)*/),
                List.of(),
                MineraculousSoundEvents.LADYBUG_TRANSFORM));
    }

    public static List<ItemStack> getMiraculousForAll(HolderLookup.Provider access) {
        List<ItemStack> list = new ArrayList<>();
        access.lookupOrThrow(MineraculousRegistries.MIRACULOUS).listElements().forEach(ref -> {
            ItemStack stack = MineraculousItems.MIRACULOUS.get().getDefaultInstance();
            stack.set(MineraculousDataComponents.MIRACULOUS, ref.key());
            list.add(stack);
        });
        return list;
    }

    public static List<ItemStack> getArmorForAll(HolderLookup.Provider access) {
        List<ItemStack> list = new ArrayList<>();
        access.lookupOrThrow(MineraculousRegistries.MIRACULOUS).listElements().forEach(ref -> {
            MineraculousArmors.MIRACULOUS.getAll().forEach(item -> {
                ItemStack stack = item.get().getDefaultInstance();
                stack.set(MineraculousDataComponents.MIRACULOUS, ref.key());
                list.add(stack);
            });
        });
        return list;
    }
}
