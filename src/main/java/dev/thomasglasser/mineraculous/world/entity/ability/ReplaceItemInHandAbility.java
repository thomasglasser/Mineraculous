package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionItemData;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ReplaceItemInHandAbility(ItemStack replacement, boolean breakOriginal, Optional<ItemPredicate> validItems, Optional<ItemPredicate> invalidItems) implements Ability {

    public static final MapCodec<ReplaceItemInHandAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("replacement", ItemStack.EMPTY).forGetter(ReplaceItemInHandAbility::replacement),
            Codec.BOOL.optionalFieldOf("break_original", false).forGetter(ReplaceItemInHandAbility::breakOriginal),
            ItemPredicate.CODEC.optionalFieldOf("valid_items").forGetter(ReplaceItemInHandAbility::validItems),
            ItemPredicate.CODEC.optionalFieldOf("invalid_items").forGetter(ReplaceItemInHandAbility::invalidItems)).apply(instance, ReplaceItemInHandAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context == null && performer instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getMainHandItem();
            if (stack.isEmpty() || validItems.map(predicate -> !predicate.test(stack)).orElse(false) || invalidItems.map(predicate -> predicate.test(stack)).orElse(false)) {
                return false;
            }
            ItemStack replacement = this.replacement.copy();
            UUID id = UUID.randomUUID();
            replacement.set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
            AbilityReversionItemData.get(level).putRecoverable(performer.getUUID(), id, stack);
            if (breakOriginal) {
                if (stack.isDamageableItem()) {
                    MineraculousEntityEvents.hurtAndBreak(stack, stack.getMaxDamage(), level, livingEntity, EquipmentSlot.MAINHAND);
                } else {
                    MineraculousEntityEvents.checkKamikotizationStack(stack, level, performer);
                }
            }
            livingEntity.setItemInHand(InteractionHand.MAIN_HAND, replacement);
            return true;
        }
        return false;
    }

    @Override
    public void revert(AbilityData data, ServerLevel level, Entity performer) {
        AbilityReversionItemData.get(level).markReverted(performer.getUUID());
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.REPLACE_ITEMS_IN_HAND.get();
    }
}
