package dev.thomasglasser.mineraculous.network;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.Mineraculous;
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

public record ClientboundSyncInventoryPayload(UUID uuid, NonNullList<ItemStack> items, NonNullList<ItemStack> armor, NonNullList<ItemStack> offhand, int selected) implements ExtendedPacketPayload {

    public static final Type<ClientboundSyncInventoryPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_inventory"));
    private static final StreamCodec<RegistryFriendlyByteBuf, NonNullList<ItemStack>> NON_NULL_LIST_ITEMSTACK_STREAM_CODEC = ItemStack.OPTIONAL_LIST_STREAM_CODEC.map(NonNullList::copyOf, Function.identity());
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncInventoryPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ClientboundSyncInventoryPayload::uuid,
            NON_NULL_LIST_ITEMSTACK_STREAM_CODEC, ClientboundSyncInventoryPayload::items,
            NON_NULL_LIST_ITEMSTACK_STREAM_CODEC, ClientboundSyncInventoryPayload::armor,
            NON_NULL_LIST_ITEMSTACK_STREAM_CODEC, ClientboundSyncInventoryPayload::offhand,
            ByteBufCodecs.INT, ClientboundSyncInventoryPayload::selected,
            ClientboundSyncInventoryPayload::new);
    public ClientboundSyncInventoryPayload(Player player) {
        this(player.getUUID(), player.getInventory().items, player.getInventory().armor, player.getInventory().offhand, player.getInventory().selected);
    }

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(uuid);
        if (target != null) {
            target.getInventory().items = this.items;
            target.getInventory().armor = this.armor;
            target.getInventory().offhand = this.offhand;
            target.getInventory().compartments = ImmutableList.of(items, armor, offhand);
            target.getInventory().selected = this.selected;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
