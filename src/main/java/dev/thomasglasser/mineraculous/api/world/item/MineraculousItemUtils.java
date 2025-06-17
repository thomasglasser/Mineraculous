package dev.thomasglasser.mineraculous.api.world.item;

import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.function.Consumer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MineraculousItemUtils {
    public static final String ITEM_UNBREAKABLE_KEY = "mineraculous.item_unbreakable";

    public static Pair<ItemStack, ItemStack> tryBreakItem(ItemStack stack, ServerLevel serverLevel, Vec3 pos, @Nullable LivingEntity breaker) {
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
                player.displayClientMessage(Component.translatable(ITEM_UNBREAKABLE_KEY), true);
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
                hurtAndBreak(stack, damage, serverLevel, breaker, EquipmentSlot.MAINHAND);
            } else {
                Kamikotization.checkBroken(stack, serverLevel, pos);
                stack.shrink(1);
                serverLevel.playSound(null, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1f, 1f);
            }
        }
        return Pair.of(stack, rest);
    }

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
}
