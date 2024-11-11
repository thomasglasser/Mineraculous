package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import java.util.Optional;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ReplaceItemsInHandAbility(ItemStack replacement, Optional<ItemPredicate> validItems, Optional<ItemPredicate> invalidItems) implements Ability {

    public static final MapCodec<ReplaceItemsInHandAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("replacement", ItemStack.EMPTY).forGetter(ReplaceItemsInHandAbility::replacement),
            ItemPredicate.CODEC.optionalFieldOf("valid_items").forGetter(ReplaceItemsInHandAbility::validItems),
            ItemPredicate.CODEC.optionalFieldOf("invalid_items").forGetter(ReplaceItemsInHandAbility::invalidItems)).apply(instance, ReplaceItemsInHandAbility::new));
    @Override
    public boolean perform(ResourceKey<Miraculous> type, MiraculousData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.INTERACT_ITEM) {
            ItemStack stack = performer.getMainHandItem();
            if (validItems.isPresent() && !validItems.get().test(stack))
                return false;
            if (invalidItems.isPresent() && invalidItems.get().test(stack))
                return false;
            performer.setItemInHand(InteractionHand.MAIN_HAND, replacement.copyWithCount(stack.getCount()));
            return true;
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.REPLACE_ITEMS_IN_HAND.get();
    }
}
