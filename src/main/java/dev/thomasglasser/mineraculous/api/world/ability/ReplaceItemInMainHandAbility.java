package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
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

/**
 * Replaces the item in the entity's main hand with the provided {@link ItemStack}.
 *
 * @param replacement   The {@link ItemStack} to replace the main hand item with
 * @param breakOriginal Whether the original item should be broken or simply replaced
 * @param validItems    The {@link ItemPredicate} the main item must match
 * @param invalidItems  The {@link ItemPredicate} the main item must not match
 * @param replaceSound  The sound to play when the main hand item is replaced
 */
public record ReplaceItemInMainHandAbility(ItemStack replacement, boolean breakOriginal, Optional<ItemPredicate> validItems, Optional<ItemPredicate> invalidItems, Optional<Holder<SoundEvent>> replaceSound) implements Ability {

    public static final MapCodec<ReplaceItemInMainHandAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("replacement", ItemStack.EMPTY).forGetter(ReplaceItemInMainHandAbility::replacement),
            Codec.BOOL.optionalFieldOf("break_original", false).forGetter(ReplaceItemInMainHandAbility::breakOriginal),
            ItemPredicate.CODEC.optionalFieldOf("valid_items").forGetter(ReplaceItemInMainHandAbility::validItems),
            ItemPredicate.CODEC.optionalFieldOf("invalid_items").forGetter(ReplaceItemInMainHandAbility::invalidItems),
            SoundEvent.CODEC.optionalFieldOf("replace_sound").forGetter(ReplaceItemInMainHandAbility::replaceSound)).apply(instance, ReplaceItemInMainHandAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context == null && performer instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getMainHandItem();
            if (stack.isEmpty() || validItems.map(predicate -> !predicate.test(stack)).orElse(false) || invalidItems.map(predicate -> predicate.test(stack)).orElse(false)) {
                return false;
            }
            ItemStack replacement = this.replacement.copy();
            UUID id = UUID.randomUUID();
            replacement.set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
            AbilityReversionItemData.get(level).putRevertible(performer.getUUID(), id, stack);
            if (breakOriginal) {
                if (stack.isDamageableItem()) {
                    MineraculousItemUtils.hurtAndBreak(stack, stack.getMaxDamage(), level, livingEntity, EquipmentSlot.MAINHAND);
                } else {
                    Kamikotization.checkBroken(stack, level, performer);
                }
            }
            livingEntity.setItemInHand(InteractionHand.MAIN_HAND, replacement);
            Ability.playSound(level, performer, replaceSound);
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
