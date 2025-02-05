package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundPutToolInHandPayload(ResourceKey<Miraculous> miraculous) implements ExtendedPacketPayload {
    public static final Type<ServerboundPutToolInHandPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_put_tool_in_hand"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundPutToolInHandPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundPutToolInHandPayload::miraculous,
            ServerboundPutToolInHandPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous);
        if (data.transformed()) {
            UUID uuid = data.miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get()).uuid();
            if (uuid != null) {
                ItemStack defaultTool = player.level().holderOrThrow(miraculous).value().tool().orElse(ItemStack.EMPTY);
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack itemstack = player.getInventory().getItem(i);
                    if (itemstack.is(defaultTool.getItem()) && itemstack.has(MineraculousDataComponents.KWAMI_DATA.get()) && itemstack.get(MineraculousDataComponents.KWAMI_DATA.get()).uuid().equals(uuid)) {
                        player.getInventory().setPickedItem(itemstack);
                        player.getInventory().setItem(i, ItemStack.EMPTY);
                        return;
                    }
                }
                CuriosUtils.getAllItems(player).forEach(((curiosData, itemStack) -> {
                    if (itemStack.is(defaultTool.getItem()) && itemStack.has(MineraculousDataComponents.KWAMI_DATA.get()) && itemStack.get(MineraculousDataComponents.KWAMI_DATA.get()).uuid().equals(uuid)) {
                        player.getInventory().setPickedItem(itemStack);
                        CuriosUtils.setStackInSlot(player, curiosData, ItemStack.EMPTY, true);
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
