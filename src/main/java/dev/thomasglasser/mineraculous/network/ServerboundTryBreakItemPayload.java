package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

public record ServerboundTryBreakItemPayload() implements ExtendedPacketPayload {
    public static final ServerboundTryBreakItemPayload INSTANCE = new ServerboundTryBreakItemPayload();
    public static final String ITEM_UNBREAKABLE_KEY = "mineraculous.item_unbreakable";

    public static final Type<ServerboundTryBreakItemPayload> TYPE = new Type<>(Mineraculous.modLoc("try_break_item"));
    public static final StreamCodec<ByteBuf, ServerboundTryBreakItemPayload> CODEC = StreamCodec.unit(INSTANCE);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack addRest = mainHandItem.copyWithCount(mainHandItem.getCount() - 1);
        mainHandItem.setCount(1);
        if (!mainHandItem.isDamageableItem()) {
            if (mainHandItem.getItem() instanceof BlockItem blockItem) {
                float max = blockItem.getBlock().defaultDestroyTime();
                if (max > -1) {
                    mainHandItem.set(DataComponents.MAX_DAMAGE, (int) (max * 100));
                    mainHandItem.set(DataComponents.DAMAGE, 0);
                    mainHandItem.set(DataComponents.MAX_STACK_SIZE, 1);
                } else {
                    mainHandItem.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
                }
            } else if (mainHandItem.is(MineraculousItemTags.TOUGH)) {
                mainHandItem.set(DataComponents.MAX_DAMAGE, 200);
                mainHandItem.set(DataComponents.DAMAGE, 0);
                mainHandItem.set(DataComponents.MAX_STACK_SIZE, 1);
            }
        }
        if (mainHandItem.has(DataComponents.UNBREAKABLE)) {
            player.displayClientMessage(Component.translatable(ITEM_UNBREAKABLE_KEY), true);
        } else {
            if (mainHandItem.isDamageableItem()) {
                int damage = 100;
                MiraculousesData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                for (ResourceKey<Miraculous> type : data.getTransformed()) {
                    damage += 100 * data.get(type).powerLevel();
                }
                hurtAndBreak(mainHandItem, damage, serverLevel, player, EquipmentSlot.MAINHAND);
            } else {
                MineraculousEntityEvents.checkKamikotizationStack(mainHandItem, serverLevel, player);
                mainHandItem.shrink(1);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1f, 1f);
            }
        }
        if (mainHandItem.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, addRest);
        } else {
            player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);
            player.addItem(addRest);
        }
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
                MineraculousEntityEvents.checkKamikotizationStack(stack, level, breaker);
                Item item = stack.getItem();
                stack.shrink(1);
                itemConsumer.accept(item);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
