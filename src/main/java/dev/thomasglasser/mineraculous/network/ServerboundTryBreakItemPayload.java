package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundTryBreakItemPayload() implements ExtendedPacketPayload {
    public static final ServerboundTryBreakItemPayload INSTANCE = new ServerboundTryBreakItemPayload();
    public static final String ITEM_UNBREAKABLE_KEY = "mineraculous.item_unbreakable";

    public static final Type<ServerboundTryBreakItemPayload> TYPE = new Type<>(Mineraculous.modLoc("try_break_item"));
    public static final StreamCodec<ByteBuf, ServerboundTryBreakItemPayload> CODEC = StreamCodec.unit(INSTANCE);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isDamageableItem()) {
            int i = 100;
            MiraculousDataSet data = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
            for (MiraculousType type : data.getTransformed()) {
                int powerLevel = data.get(type).powerLevel();
                if (powerLevel > 0)
                    i *= powerLevel;
            }
            mainHandItem.hurtAndBreak(i, player, EquipmentSlot.MAINHAND);
        } else if (mainHandItem.has(DataComponents.UNBREAKABLE)) {
            player.displayClientMessage(Component.translatable(ITEM_UNBREAKABLE_KEY), true);
        } else {
            mainHandItem.shrink(1);
        }
        // TODO: Release akuma if inside
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
