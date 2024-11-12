package dev.thomasglasser.mineraculous.world.entity.kamikotization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record Kamikotization(Component name, List<String> includedLooks, ItemPredicate itemPredicate, List<Holder<Ability>> abilities) {

    public static final Codec<Kamikotization> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Kamikotization::name),
            Codec.STRING.listOf().fieldOf("included_looks").forGetter(Kamikotization::includedLooks),
            ItemPredicate.CODEC.fieldOf("item_predicate").forGetter(Kamikotization::itemPredicate),
            Ability.CODEC.listOf().fieldOf("abilities").forGetter(Kamikotization::abilities)).apply(instance, Kamikotization::new));
    public static Set<Holder<Kamikotization>> getFor(Player player) {
        Set<Holder<Kamikotization>> kamikotizations = new HashSet<>();
        for (Holder<Kamikotization> kamikotization : player.level().registryAccess().lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION).listElements().toList()) {
            Inventory inventory = player.getInventory();
            for (NonNullList<ItemStack> stacks : inventory.compartments) {
                for (ItemStack stack : stacks) {
                    if (!stack.isEmpty() && kamikotization.value().itemPredicate().test(stack)) {
                        kamikotizations.add(kamikotization);
                        break;
                    }
                }
            }
        }
        return kamikotizations;
    }

    public static ItemStack createItemStack(Item item, ResourceKey<Kamikotization> key) {
        ItemStack stack = new ItemStack(item);
        stack.set(MineraculousDataComponents.KAMIKOTIZATION, key);
        return stack;
    }

    public static List<ItemStack> getArmorForAll(HolderLookup.Provider access) {
        List<ItemStack> list = new ArrayList<>();
        access.lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION).listElements().forEach(ref -> {
            MineraculousArmors.KAMIKOTIZATION.getAll().forEach(item -> {
                ItemStack stack = item.get().getDefaultInstance();
                stack.set(MineraculousDataComponents.KAMIKOTIZATION, ref.key());
                list.add(stack);
            });
        });
        return list;
    }
}
