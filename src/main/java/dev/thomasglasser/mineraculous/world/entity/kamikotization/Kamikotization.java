package dev.thomasglasser.mineraculous.world.entity.kamikotization;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IHolderExtension;

public record Kamikotization(String defaultName, ItemPredicate itemPredicate, Either<ItemStack, Holder<Ability>> powerSource, List<Holder<Ability>> passiveAbilities) {

    public static final String NO_KAMIKOTIZATIONS = "mineraculous.no_kamikotizations";
    public static final Codec<Kamikotization> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("default_name").forGetter(Kamikotization::defaultName),
            ItemPredicate.CODEC.optionalFieldOf("item_predicate", ItemPredicate.Builder.item().build()).forGetter(Kamikotization::itemPredicate),
            new EitherCodec<>(ItemStack.CODEC, Ability.CODEC).fieldOf("power_source").forGetter(Kamikotization::powerSource),
            Ability.CODEC.listOf().optionalFieldOf("passive_abilities", List.of()).forGetter(Kamikotization::passiveAbilities)).apply(instance, Kamikotization::new));
    @Override
    public Either<ItemStack, Holder<Ability>> powerSource() {
        return powerSource.mapLeft(ItemStack::copy);
    }

    public static Set<Holder<Kamikotization>> getFor(Player player) {
        Set<Holder<Kamikotization>> kamikotizations = new HashSet<>();
        for (Holder<Kamikotization> kamikotization : player.level().registryAccess().lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION).listElements().toList()) {
            Inventory inventory = player.getInventory();
            for (NonNullList<ItemStack> stacks : inventory.compartments) {
                for (ItemStack stack : stacks) {
                    if (!stack.isEmpty() && !stack.has(MineraculousDataComponents.KAMIKOTIZATION) && kamikotization.value().itemPredicate().test(stack)) {
                        kamikotizations.add(kamikotization);
                        break;
                    }
                }
            }
        }
        return kamikotizations.stream().sorted(Comparator.comparing(IHolderExtension::getKey)).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static ItemStack createItemStack(Item item, ResourceKey<Kamikotization> key) {
        ItemStack stack = new ItemStack(item);
        stack.set(MineraculousDataComponents.KAMIKOTIZATION, key);
        return stack;
    }
}
