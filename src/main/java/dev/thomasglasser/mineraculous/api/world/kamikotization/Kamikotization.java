package dev.thomasglasser.mineraculous.api.world.kamikotization;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Data-driven entity transformation provided by {@link Kamiko} control.
 *
 * @param defaultName      The default name of the kamikotized entity
 * @param itemPredicate    The predicate for which items be used to transform
 * @param powerSource      The {@link ItemStack} tool or {@link Ability} that provides the main power
 * @param passiveAbilities Passive {@link Ability}s
 */
public record Kamikotization(String defaultName, ItemPredicate itemPredicate, Either<ItemStack, Holder<Ability>> powerSource, HolderSet<Ability> passiveAbilities) {

    public static final int TRANSFORMATION_FRAMES = 10;
    public static final String NO_KAMIKOTIZATIONS = "mineraculous.no_kamikotizations";

    public static final Codec<Kamikotization> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("default_name").forGetter(Kamikotization::defaultName),
            ItemPredicate.CODEC.optionalFieldOf("item_predicate", ItemPredicate.Builder.item().build()).forGetter(Kamikotization::itemPredicate),
            new EitherCodec<>(ItemStack.CODEC, Ability.CODEC).fieldOf("power_source").forGetter(Kamikotization::powerSource),
            Ability.HOLDER_SET_CODEC.optionalFieldOf("passive_abilities", HolderSet.empty()).forGetter(Kamikotization::passiveAbilities)).apply(instance, Kamikotization::new));
    public static final Codec<Holder<Kamikotization>> CODEC = RegistryFileCodec.create(MineraculousRegistries.KAMIKOTIZATION, DIRECT_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, Kamikotization> DIRECT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Kamikotization::defaultName,
            ByteBufCodecs.fromCodecWithRegistries(ItemPredicate.CODEC), Kamikotization::itemPredicate,
            ByteBufCodecs.either(ItemStack.STREAM_CODEC, Ability.STREAM_CODEC), Kamikotization::powerSource,
            Ability.HOLDER_SET_STREAM_CODEC, Kamikotization::passiveAbilities,
            Kamikotization::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<Kamikotization>> STREAM_CODEC = ByteBufCodecs.holder(MineraculousRegistries.KAMIKOTIZATION, DIRECT_STREAM_CODEC);

    // Ensure the original ItemStack is never messed with
    @Override
    public Either<ItemStack, Holder<Ability>> powerSource() {
        return powerSource.mapLeft(ItemStack::copy);
    }

    /**
     * Compiles a list of all valid Kamikotizations for the provided {@link Entity}'s inventory.
     *
     * @param entity The entity to compile Kamikotizations for
     * @return The compiled list of valid Kamikotizations
     */
    public static List<Holder<Kamikotization>> getFor(Entity entity) {
        List<Holder<Kamikotization>> kamikotizations = new ReferenceArrayList<>();
        entity.level().registryAccess().lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION).listElements().forEach(kamikotization -> {
            for (ItemStack stack : EntityUtils.getInventory(entity)) {
                if (!stack.isEmpty() && !stack.has(MineraculousDataComponents.KAMIKOTIZATION) && kamikotization.value().itemPredicate().test(stack)) {
                    kamikotizations.add(kamikotization);
                    break;
                }
            }
        });
        return kamikotizations;
    }

    /**
     * Creates an {@link ItemStack} with the provided {@link Kamikotization} component.
     *
     * @param item           The item to put the Kamikotization on
     * @param kamikotization The Kamikotization to put on the item
     * @return The item with the Kamikotization
     */
    public static ItemStack createItemStack(ItemLike item, Holder<Kamikotization> kamikotization) {
        ItemStack stack = item.asItem().getDefaultInstance();
        stack.set(MineraculousDataComponents.KAMIKOTIZATION, kamikotization);
        return stack;
    }

    /**
     * Checks the provided broken {@link ItemStack} for Kamikotization
     * and decrements the stack count or detransforms the owner.
     *
     * @param stack   The broken stack to check
     * @param level   The level to perform the detransformation in
     * @param breaker The breaker of the item
     */
    public static void checkBroken(ItemStack stack, ServerLevel level, @Nullable Entity breaker) {
        checkBroken(stack, level, breaker != null ? breaker.position().add(0, 1, 0) : null);
    }

    /**
     * Checks the provided broken {@link ItemStack} for Kamikotization
     * and decrements the stack count or detransforms the owner.
     *
     * @param stack     The broken stack to check
     * @param level     The level to perform the detransformation in
     * @param kamikoPos The position to spawn the {@link Kamiko} upon detransformation
     */
    public static void checkBroken(ItemStack stack, ServerLevel level, @Nullable Vec3 kamikoPos) {
        Holder<Kamikotization> kamikotization = stack.get(MineraculousDataComponents.KAMIKOTIZATION);
        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
        if (kamikotization != null && ownerId != null) {
            Entity owner = level.getEntity(ownerId);
            if (owner != null) {
                owner.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresentOrElse(data -> {
                    if (data.remainingStackCount() <= 1) {
                        data.detransform(owner, level, kamikoPos != null ? kamikoPos : owner.position().add(0, 1, 0), false);
                    } else {
                        data.decrementRemainingStackCount().save(owner, true);
                    }
                }, () -> stack.setCount(0));
            }
        }
    }
}
