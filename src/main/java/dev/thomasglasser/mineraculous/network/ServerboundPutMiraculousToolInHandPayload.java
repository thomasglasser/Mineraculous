package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundPutMiraculousToolInHandPayload(ResourceKey<Miraculous> miraculous) implements ExtendedPacketPayload {
    public static final Type<ServerboundPutMiraculousToolInHandPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_put_miraculous_tool_in_hand"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPutMiraculousToolInHandPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundPutMiraculousToolInHandPayload::miraculous,
            ServerboundPutMiraculousToolInHandPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        if (player.getMainHandItem().isEmpty()) {
            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous);
            KwamiData kwamiData = data.miraculousItem().get(MineraculousDataComponents.KWAMI_DATA);
            UUID uuid = kwamiData != null ? kwamiData.uuid() : null;
            if (uuid != null) {
                ItemStack defaultTool = player.level().holderOrThrow(miraculous).value().tool().orElse(ItemStack.EMPTY);
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
