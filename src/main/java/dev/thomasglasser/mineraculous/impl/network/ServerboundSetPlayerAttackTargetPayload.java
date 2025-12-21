package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetPlayerAttackTargetPayload(int entityId, UUID targetId) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetPlayerAttackTargetPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_set_attack_target"));
    public static final StreamCodec<ByteBuf, ServerboundSetPlayerAttackTargetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ServerboundSetPlayerAttackTargetPayload::entityId,
            UUIDUtil.STREAM_CODEC, ServerboundSetPlayerAttackTargetPayload::targetId,
            ServerboundSetPlayerAttackTargetPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(targetId);
        if (player.level().getEntity(entityId) instanceof Mob mob && target != null) {
            mob.setTarget(target);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
