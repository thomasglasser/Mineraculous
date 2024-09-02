package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ReplaceItemsInHandAbility(ItemStack replacement, Optional<HolderSet<Item>> validItems, Optional<HolderSet<Item>> invalidItems) implements Ability {

    public static final MapCodec<ReplaceItemsInHandAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("replacement", ItemStack.EMPTY).forGetter(ReplaceItemsInHandAbility::replacement),
            RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("valid_items").forGetter(ReplaceItemsInHandAbility::validItems),
            RegistryCodecs.homogeneousList(Registries.ITEM).optionalFieldOf("invalid_items").forGetter(ReplaceItemsInHandAbility::invalidItems)).apply(instance, ReplaceItemsInHandAbility::new));
    @Override
    public boolean perform(ResourceKey<Miraculous> type, MiraculousData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.INTERACT_ITEM) {
            ItemStack stack = performer.getMainHandItem();
            if (validItems.isPresent() && !validItems.get().contains(stack.getItem().builtInRegistryHolder())) return true;
            if (invalidItems.isPresent() && invalidItems.get().contains(stack.getItem().builtInRegistryHolder())) return true;
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
