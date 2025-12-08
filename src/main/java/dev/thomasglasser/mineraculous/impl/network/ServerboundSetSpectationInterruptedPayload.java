package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
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
    public static final Type<ServerboundSetSpectationInterruptedPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_set_spectation_interrupted"));
    public static final StreamCodec<ByteBuf, ServerboundSetSpectationInterruptedPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), ServerboundSetSpectationInterruptedPayload::targetId,
            ServerboundSetSpectationInterruptedPayload::new);

    @Override
    public void handle(Player player) {
        Entity target = targetId.isPresent() ? player.level().getEntity(targetId.get()) : player;
        if (target != null) {
            target.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withSpectationInterrupted(true).save(target);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
