package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;

public record ServerboundTryBreakItemPayload() implements ExtendedPacketPayload {
    public static final ServerboundTryBreakItemPayload INSTANCE = new ServerboundTryBreakItemPayload();
    public static final String ITEM_UNBREAKABLE_KEY = "mineraculous.item_unbreakable";

    public static final Type<ServerboundTryBreakItemPayload> TYPE = new Type<>(Mineraculous.modLoc("try_break_item"));
    public static final StreamCodec<ByteBuf, ServerboundTryBreakItemPayload> CODEC = StreamCodec.unit(INSTANCE);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack addRest = mainHandItem.copyWithCount(mainHandItem.getCount() - 1);
        mainHandItem.setCount(1);
        if (mainHandItem.isDamageableItem()) {
            int i = 100;
            MiraculousDataSet data = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
            for (ResourceKey<Miraculous> type : data.getTransformed()) {
                int powerLevel = data.get(type).powerLevel();
                if (powerLevel > 0)
                    i *= powerLevel;
            }
            mainHandItem.hurtAndBreak(i, player, EquipmentSlot.MAINHAND);
        } else if (mainHandItem.has(DataComponents.UNBREAKABLE)) {
            player.displayClientMessage(Component.translatable(ITEM_UNBREAKABLE_KEY), true);
            return;
        } else if (mainHandItem.getItem() instanceof BlockItem blockItem) {
            float max = blockItem.getBlock().defaultDestroyTime();
            if (max > -1) {
                mainHandItem.set(DataComponents.MAX_DAMAGE, (int) (max * 100.0));
                mainHandItem.set(DataComponents.DAMAGE, 0);
                mainHandItem.set(DataComponents.MAX_STACK_SIZE, 1);
                mainHandItem.hurtAndBreak(100, player, EquipmentSlot.MAINHAND);
            } else {
                mainHandItem.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
                return;
            }
        } else if (mainHandItem.is(MineraculousItemTags.TOUGH)) {
            mainHandItem.set(DataComponents.MAX_DAMAGE, 2);
            mainHandItem.set(DataComponents.DAMAGE, 0);
            mainHandItem.set(DataComponents.MAX_STACK_SIZE, 1);
            mainHandItem.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        } else {
            if (mainHandItem.has(MineraculousDataComponents.KAMIKOTIZATION) && mainHandItem.has(DataComponents.PROFILE)) {
                ServerPlayer target = (ServerPlayer) player.level().getPlayerByUUID(mainHandItem.get(DataComponents.PROFILE).gameProfile().getId());
                if (target != null)
                    MineraculousEntityEvents.handleKamikotizationTransformation(target, target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow(), false, false, true, player.position().add(0, 1, 0));
            }
            mainHandItem.shrink(1);
            player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1f, 1f);
        }
        if (!mainHandItem.isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);
            player.addItem(addRest);
        } else {
            player.setItemInHand(InteractionHand.MAIN_HAND, addRest);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
