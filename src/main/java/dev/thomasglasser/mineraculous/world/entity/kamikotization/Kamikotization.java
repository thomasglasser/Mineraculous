package dev.thomasglasser.mineraculous.world.entity.kamikotization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record Kamikotization(String defaultName, ItemPredicate itemPredicate, List<Holder<Ability>> abilities) {

    public static final Codec<Kamikotization> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("default_name").forGetter(Kamikotization::defaultName),
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
}
