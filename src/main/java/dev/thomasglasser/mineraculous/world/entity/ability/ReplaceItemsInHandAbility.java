package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryDataHolder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record ReplaceItemsInHandAbility(ItemStack replacement, boolean hurtAndBreak, Optional<ItemPredicate> validItems, Optional<ItemPredicate> invalidItems, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final MapCodec<ReplaceItemsInHandAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("replacement", ItemStack.EMPTY).forGetter(ReplaceItemsInHandAbility::replacement),
            Codec.BOOL.optionalFieldOf("hurt_and_break", false).forGetter(ReplaceItemsInHandAbility::hurtAndBreak),
            ItemPredicate.CODEC.optionalFieldOf("valid_items").forGetter(ReplaceItemsInHandAbility::validItems),
            ItemPredicate.CODEC.optionalFieldOf("invalid_items").forGetter(ReplaceItemsInHandAbility::invalidItems),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(ReplaceItemsInHandAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(ReplaceItemsInHandAbility::overrideActive)).apply(instance, ReplaceItemsInHandAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.INTERACT_ITEM) {
            ItemStack stack = context.stack();
            if (validItems.isPresent() && !validItems.get().test(stack))
                return false;
            if (invalidItems.isPresent() && invalidItems.get().test(stack))
                return false;
            ItemStack replacement = this.replacement.copyWithCount(1);
            UUID id = UUID.randomUUID();
            replacement.set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
            ((MiraculousRecoveryDataHolder) level.getServer().overworld()).mineraculous$getMiraculousRecoveryItemData().putRecoverable(entity.getUUID(), id, stack);
            stack.setCount(0);
            if (hurtAndBreak) {
                if (stack.isDamageableItem()) {
                    stack.hurtAndBreak(stack.getMaxDamage(), entity, EquipmentSlot.MAINHAND);
                } else {
                    if (stack.has(MineraculousDataComponents.KAMIKOTIZATION) && stack.has(DataComponents.PROFILE)) {
                        ServerPlayer target = (ServerPlayer) entity.level().getPlayerByUUID(stack.get(DataComponents.PROFILE).gameProfile().getId());
                        if (target != null) {
                            KamikotizationData kamikotizationData = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow();
                            if (kamikotizationData.stackCount() <= 1)
                                MineraculousEntityEvents.handleKamikotizationTransformation(target, kamikotizationData, false, false, entity.position().add(0, 1, 0));
                            else {
                                kamikotizationData.decrementStackCount().save(target, true);
                            }
                        }
                    }
                }
            }
            if (context.entity() instanceof LivingEntity livingEntity)
                livingEntity.setItemInHand(livingEntity.getUsedItemHand(), replacement);
            else
                entity.setItemInHand(InteractionHand.MAIN_HAND, replacement);
            playStartSound(level, pos);
            return true;
        }
        return false;
    }

    @Override
    public void restore(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        ((MiraculousRecoveryDataHolder) level.getServer().overworld()).mineraculous$getMiraculousRecoveryItemData().markRecovered(entity.getUUID());
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.REPLACE_ITEMS_IN_HAND.get();
    }
}
