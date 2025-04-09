package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundPutKamikotizationToolInHandPayload() implements ExtendedPacketPayload {
    public static final ServerboundPutKamikotizationToolInHandPayload INSTANCE = new ServerboundPutKamikotizationToolInHandPayload();
    public static final Type<ServerboundPutKamikotizationToolInHandPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_put_kamikotization_tool_in_hand"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPutKamikotizationToolInHandPayload> CODEC = StreamCodec.unit(INSTANCE);

    // ON SERVER
    @Override
    public void handle(Player player) {
        player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
            UUID uuid = data.kamikoData().uuid();
            if (uuid != null) {
                ItemStack defaultTool = player.level().holderOrThrow(data.kamikotization()).value().powerSource().left().orElse(ItemStack.EMPTY);
                if (defaultTool.isEmpty())
                    return;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack itemstack = player.getInventory().getItem(i);
                    if (itemstack.is(defaultTool.getItem()) && itemstack.has(MineraculousDataComponents.KAMIKO_DATA.get()) && itemstack.get(MineraculousDataComponents.KAMIKO_DATA.get()).uuid().equals(uuid)) {
                        player.getInventory().setPickedItem(itemstack);
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                        return;
                    }
                }
                CuriosUtils.getAllItems(player).forEach(((curiosData, itemStack) -> {
                    if (itemStack.is(defaultTool.getItem()) && itemStack.has(MineraculousDataComponents.KAMIKO_DATA.get()) && itemStack.get(MineraculousDataComponents.KAMIKO_DATA.get()).uuid().equals(uuid)) {
                        player.getInventory().setPickedItem(itemStack);
                        CuriosUtils.setStackInSlot(player, curiosData, ItemStack.EMPTY);
                    }
                }));
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
