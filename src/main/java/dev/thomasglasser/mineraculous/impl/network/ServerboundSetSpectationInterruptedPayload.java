package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetSpectationInterruptedPayload(Optional<Integer> targetId) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetSpectationInterruptedPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_spectation_interrupted"));
    public static final StreamCodec<ByteBuf, ServerboundSetSpectationInterruptedPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT), ServerboundSetSpectationInterruptedPayload::targetId,
            ServerboundSetSpectationInterruptedPayload::new);

    @Override
    public void handle(Player player) {
        Entity target = targetId.map(player.level()::getEntity).orElse(player);
        target.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withSpectationInterrupted().save(target, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
