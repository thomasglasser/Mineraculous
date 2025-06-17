package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.component.KamikoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundPutKamikotizationToolInHandPayload() implements ExtendedPacketPayload {
    public static final ServerboundPutKamikotizationToolInHandPayload INSTANCE = new ServerboundPutKamikotizationToolInHandPayload();
    public static final Type<ServerboundPutKamikotizationToolInHandPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_put_kamikotization_tool_in_hand"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPutKamikotizationToolInHandPayload> CODEC = StreamCodec.unit(INSTANCE);

    // ON SERVER
    @Override
    public void handle(Player player) {
        if (player.getMainHandItem().isEmpty()) {
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.kamikotization().value().powerSource().left().ifPresent(defaultTool -> {
                if (defaultTool.isEmpty())
                    return;
                UUID uuid = data.kamikoData().uuid();
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    KamikoData kamikoData = stack.get(MineraculousDataComponents.KAMIKO_DATA);
                    if (kamikoData != null && stack.is(defaultTool.getItem()) && kamikoData.uuid().equals(uuid)) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                        return;
                    }
                }
                CuriosUtils.getAllItems(player).forEach(((curiosData, stack) -> {
                    KamikoData kamikoData = stack.get(MineraculousDataComponents.KAMIKO_DATA);
                    if (kamikoData != null && stack.is(defaultTool.getItem()) && kamikoData.uuid().equals(uuid)) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                        CuriosUtils.setStackInSlot(player, curiosData, ItemStack.EMPTY);
                    }
                }));
            }));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
