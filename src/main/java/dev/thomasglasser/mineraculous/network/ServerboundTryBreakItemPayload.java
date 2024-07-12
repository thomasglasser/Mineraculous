package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
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
        ItemStack addRest = player.getMainHandItem().copyWithCount(player.getMainHandItem().getCount() - 1);
        player.getMainHandItem().setCount(1);
        if (player.getMainHandItem().isDamageableItem()) {
            int i = 100;
            MiraculousDataSet data = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
            for (MiraculousType type : data.getTransformed()) {
                int powerLevel = data.get(type).powerLevel();
                if (powerLevel > 0)
                    i *= powerLevel;
            }
            player.getMainHandItem().hurtAndBreak(i, player, EquipmentSlot.MAINHAND);
        } else if (player.getMainHandItem().has(DataComponents.UNBREAKABLE)) {
            player.displayClientMessage(Component.translatable(ITEM_UNBREAKABLE_KEY), true);
        } else if (player.getMainHandItem().getItem() instanceof BlockItem blockItem) {
            float max = blockItem.getBlock().defaultDestroyTime();
            if (max > -1) {
                player.getMainHandItem().set(DataComponents.MAX_DAMAGE, (int) (max * 100.0));
                player.getMainHandItem().set(DataComponents.DAMAGE, 0);
                player.getMainHandItem().set(DataComponents.MAX_STACK_SIZE, 1);
                player.getMainHandItem().hurtAndBreak(100, player, EquipmentSlot.MAINHAND);
            } else {
                player.getMainHandItem().set(DataComponents.UNBREAKABLE, new Unbreakable(false));
            }
        } else if (player.getMainHandItem().is(MineraculousItemTags.TOUGH)) {
            player.getMainHandItem().set(DataComponents.MAX_DAMAGE, 2);
            player.getMainHandItem().set(DataComponents.DAMAGE, 0);
            player.getMainHandItem().set(DataComponents.MAX_STACK_SIZE, 1);
            player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        } else {
            player.getMainHandItem().shrink(1);
            player.playSound(SoundEvents.ITEM_BREAK);
        }
        player.addItem(addRest);
        // TODO: Release akuma if inside
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
