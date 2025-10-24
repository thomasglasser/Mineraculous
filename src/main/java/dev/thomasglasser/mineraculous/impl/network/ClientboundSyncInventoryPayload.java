package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ClientboundSyncInventoryPayload(UUID uuid, NonNullList<ItemStack> inventory, int selected) implements ExtendedPacketPayload {

    public static final Type<ClientboundSyncInventoryPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_sync_inventory"));
    private static final StreamCodec<RegistryFriendlyByteBuf, NonNullList<ItemStack>> NON_NULL_LIST_ITEMSTACK_STREAM_CODEC = ItemStack.OPTIONAL_LIST_STREAM_CODEC.map(stacks -> NonNullList.of(ItemStack.EMPTY, stacks.toArray(ItemStack[]::new)), Function.identity());
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncInventoryPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ClientboundSyncInventoryPayload::uuid,
            NON_NULL_LIST_ITEMSTACK_STREAM_CODEC, ClientboundSyncInventoryPayload::inventory,
            ByteBufCodecs.INT, ClientboundSyncInventoryPayload::selected,
            ClientboundSyncInventoryPayload::new);
    public ClientboundSyncInventoryPayload(Player player) {
        this(player.getUUID(), getInventory(player), player.getInventory().selected);
    }

    private static NonNullList<ItemStack> getInventory(Player player) {
        NonNullList<ItemStack> inventory = NonNullList.create();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            inventory.add(player.getInventory().getItem(i));
        }
        return inventory;
    }

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(uuid);
        if (target != null && player != target) {
            target.getInventory().selected = selected;
            for (int i = 0; i < inventory.size(); i++) {
                target.getInventory().setItem(i, inventory.get(i));
            }
            MineraculousClientUtils.triggerInventorySyncTracker(target);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
