package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.api.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundPutMiraculousToolInHandPayload(Holder<Miraculous> miraculous) implements ExtendedPacketPayload {
    public static final Type<ServerboundPutMiraculousToolInHandPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_put_miraculous_tool_in_hand"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPutMiraculousToolInHandPayload> CODEC = StreamCodec.composite(
            Miraculous.STREAM_CODEC, ServerboundPutMiraculousToolInHandPayload::miraculous,
            ServerboundPutMiraculousToolInHandPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        if (player.getMainHandItem().isEmpty()) {
            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
            if (data.transformed()) {
                KwamiData kwamiData = data.kwamiData().orElse(null);
                UUID uuid = kwamiData != null ? kwamiData.uuid() : null;
                if (uuid != null) {
                    ItemStack defaultTool = miraculous.value().tool();
                    if (defaultTool.isEmpty())
                        return;
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        KwamiData stackKwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                        if (stack.is(defaultTool.getItem()) && stackKwamiData != null && stackKwamiData.uuid().equals(uuid)) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                            return;
                        }
                    }
                    CuriosUtils.getAllItems(player).forEach(((curiosData, stack) -> {
                        if (stack.is(defaultTool.getItem()) && stack.has(MineraculousDataComponents.KWAMI_DATA.get()) && stack.get(MineraculousDataComponents.KWAMI_DATA.get()).uuid().equals(uuid)) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                            CuriosUtils.setStackInSlot(player, curiosData, ItemStack.EMPTY);
                        }
                    }));
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
