package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ReplaceItemInMainHandAbility(ItemStack replacement, boolean breakOriginal, Optional<ItemPredicate> validItems, Optional<ItemPredicate> invalidItems, Optional<Holder<SoundEvent>> applySound) implements Ability {

    public static final MapCodec<ReplaceItemInMainHandAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("replacement", ItemStack.EMPTY).forGetter(ReplaceItemInMainHandAbility::replacement),
            Codec.BOOL.optionalFieldOf("break_original", false).forGetter(ReplaceItemInMainHandAbility::breakOriginal),
            ItemPredicate.CODEC.optionalFieldOf("valid_items").forGetter(ReplaceItemInMainHandAbility::validItems),
            ItemPredicate.CODEC.optionalFieldOf("invalid_items").forGetter(ReplaceItemInMainHandAbility::invalidItems),
            SoundEvent.CODEC.optionalFieldOf("apply_sound").forGetter(ReplaceItemInMainHandAbility::applySound)).apply(instance, ReplaceItemInMainHandAbility::new));
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
            AbilityReversionItemData.get(level).putRevertable(performer.getUUID(), id, stack);
            if (breakOriginal) {
                if (stack.isDamageableItem()) {
                    MineraculousItemUtils.hurtAndBreak(stack, stack.getMaxDamage(), level, livingEntity, EquipmentSlot.MAINHAND);
                } else {
                    Kamikotization.checkBroken(stack, level, performer);
                }
            }
            livingEntity.setItemInHand(InteractionHand.MAIN_HAND, replacement);
            Ability.playSound(level, performer, applySound);
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
        return AbilitySerializers.REPLACE_ITEM_IN_MAIN_HAND.get();
    }
}
