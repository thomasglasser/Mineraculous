package dev.thomasglasser.mineraculous.api.world.item;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class MineraculousItemUtils {
    public static final Component ITEM_UNBREAKABLE_KEY = Component.translatable("mineraculous.item_unbreakable");

    /**
     * Tries to break the provided {@link ItemStack}, checking its {@link Kamikotization}.
     *
     * @param stack   The stack to break
     * @param level   The level to break the stack in
     * @param pos     The position the stack is being broken at
     * @param breaker The entity breaking the stack if present
     * @return A pair of the broken stack and remainder stack
     */
    public static Pair<ItemStack, ItemStack> tryBreakItem(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker) {
        ItemStack rest = stack.copyWithCount(stack.getCount() - 1);
        stack.setCount(1);
        if (!stack.isDamageableItem()) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                float max = blockItem.getBlock().defaultDestroyTime();
                if (max > -1) {
                    stack.set(DataComponents.MAX_DAMAGE, (int) (max * 100));
                    stack.set(DataComponents.DAMAGE, 0);
                    stack.set(DataComponents.MAX_STACK_SIZE, 1);
                } else {
                    stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
                }
            } else if (stack.is(MineraculousItemTags.TOUGH)) {
                stack.set(DataComponents.MAX_DAMAGE, 200);
                stack.set(DataComponents.DAMAGE, 0);
                stack.set(DataComponents.MAX_STACK_SIZE, 1);
            }
        }
        if (stack.has(DataComponents.UNBREAKABLE)) {
            if (breaker instanceof Player player) {
                player.displayClientMessage(ITEM_UNBREAKABLE_KEY, true);
            }
        } else {
            if (stack.isDamageableItem()) {
                int damage = 100;
                if (breaker != null) {
                    MiraculousesData data = breaker.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                    for (Holder<Miraculous> miraculous : data.getTransformed()) {
                        damage += 100 * data.get(miraculous).powerLevel();
                    }
                }
                hurtAndBreak(stack, damage, level, breaker, EquipmentSlot.MAINHAND);
            } else {
                Kamikotization.checkBroken(stack, level, pos);
                stack.shrink(1);
                level.playSound(null, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1f, 1f);
            }
        }
        return Pair.of(stack, rest);
    }

    /**
     * Damages the provided damageable {@link ItemStack} and breaks it if it should be broken.
     *
     * @param stack   The damageable stack to hurt and break
     * @param damage  The amount of damage to apply to the stack
     * @param level   The level to damage the stack in
     * @param breaker The breaker of the stack if present
     * @param slot    The {@link EquipmentSlot} the stack is in if present
     */
    public static void hurtAndBreak(ItemStack stack, int damage, ServerLevel level, @Nullable LivingEntity breaker, @Nullable EquipmentSlot slot) {
        if (stack.isDamageableItem()) {
            Consumer<Item> itemConsumer = item -> {
                if (breaker != null && slot != null)
                    breaker.onEquippedItemBroken(item, slot);
            };
            damage = stack.getItem().damageItem(stack, damage, breaker, itemConsumer);

            if (damage > 0) {
                damage = EnchantmentHelper.processDurabilityChange(level, stack, damage);
                if (damage <= 0) {
                    return;
                }
            }

            if (breaker instanceof ServerPlayer serverPlayer) {
                if (damage != 0) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, stack, stack.getDamageValue() + damage);
                }
            }

            int newDamage = stack.getDamageValue() + damage;
            stack.setDamageValue(newDamage);
            if (newDamage >= stack.getMaxDamage()) {
                Kamikotization.checkBroken(stack, level, breaker);
                Item item = stack.getItem();
                stack.shrink(1);
                itemConsumer.accept(item);
            }
        }
    }

    /**
     * Slows fall and cancels damage for the entity blocking with the item pointed upwards.
     *
     * @param stack  The stack being used to block
     * @param entity The entity blocking
     */
    public static void checkHelicopterSlowFall(ItemStack stack, Entity entity) {
        if (stack.has(MineraculousDataComponents.BLOCKING) && entity.getXRot() <= -75 && entity.getDeltaMovement().y <= 0) {
            applyHelicopterSlowFall(entity);
        }
    }

    // to be used on both client and server
    public static void applyHelicopterSlowFall(Entity entity) {
        double overrideDelta = Math.max(entity.getDeltaMovement().y, -0.1);
        entity.setDeltaMovement(entity.getDeltaMovement().x, overrideDelta, entity.getDeltaMovement().z);
        entity.resetFallDistance();
    }

    /**
     * Perform an {@link Object#equals(Object)} check on two {@link PatchedDataComponentMap}s,
     * ignoring the provided {@link DataComponentType}.
     * <p>
     * This is typically only called by an internal mixin
     * <p>
     *
     * @author GeckoLib
     */
    @ApiStatus.Internal
    public static boolean isSameComponentsBesides(PatchedDataComponentMap self, PatchedDataComponentMap other, DataComponentType<?> type) {
        boolean patched = false;

        if (self.has(type)) {
            PatchedDataComponentMap prevMap = self;
            boolean copyOnWrite = prevMap.copyOnWrite;
            (self = self.copy()).remove(type);
            self.copyOnWrite = copyOnWrite;
            patched = true;
        }

        if (other.has(type)) {
            PatchedDataComponentMap prevMap = other;
            boolean copyOnWrite = prevMap.copyOnWrite;
            (other = other.copy()).remove(type);
            other.copyOnWrite = copyOnWrite;
            patched = true;
        }

        return patched && Objects.equals(self, other);
    }

    @ApiStatus.Internal
    public static <T> boolean isSameComponentsBesides(PatchedDataComponentMap self, PatchedDataComponentMap other, Supplier<DataComponentType<T>> type) {
        return isSameComponentsBesides(self, other, type.get());
    }
}
