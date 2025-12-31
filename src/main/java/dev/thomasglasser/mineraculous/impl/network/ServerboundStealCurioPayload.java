package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.event.StealEvent;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public record ServerboundStealCurioPayload(UUID target, CuriosData data) implements ExtendedPacketPayload {
    public static final Type<ServerboundStealCurioPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_steal_curio"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStealCurioPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundStealCurioPayload::target,
            CuriosData.STREAM_CODEC, ServerboundStealCurioPayload::data,
            ServerboundStealCurioPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(this.target);
        if (target != null) {
            ItemStack stack = CuriosUtils.getStackInSlot(target, this.data);
            if (NeoForge.EVENT_BUS.post(new StealEvent.Finish(player, target, stack)).isCanceled())
                return;
            CuriosUtils.setStackInSlot(target, this.data, ItemStack.EMPTY);
            ServerboundStealItemPayload.giveOrDropItem(player, stack.copyAndClear());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
