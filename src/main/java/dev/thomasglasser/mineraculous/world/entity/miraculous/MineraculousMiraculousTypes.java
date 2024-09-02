package dev.thomasglasser.mineraculous.world.entity.miraculous;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.MineraculousAbilities;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public class MineraculousMiraculousTypes {
    public static final ResourceKey<Miraculous> CAT = register("cat");

    private static ResourceKey<Miraculous> register(String id) {
        return ResourceKey.create(MineraculousRegistries.MIRACULOUS, Mineraculous.modLoc(id));
    }

    public static void bootstrap(BootstrapContext<Miraculous> context) {
        context.register(CAT, new Miraculous(
                TextColor.fromRgb(0xc6f800),
                List.of(),
                "ring",
                MineraculousItems.CAT_STAFF.get().getDefaultInstance(),
                // TODO: Add plagg hungry sound
                Optional.empty(),
                context.lookup(MineraculousRegistries.ABILITY).getOrThrow(MineraculousAbilities.CATACLYSM),
                List.of(
                        context.lookup(MineraculousRegistries.ABILITY).getOrThrow(MineraculousAbilities.CAT_VISION))));
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
