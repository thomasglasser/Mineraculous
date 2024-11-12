package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetPlayerAttackTargetPayload(int entity, UUID target) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetPlayerAttackTargetPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_attack_target"));
    public static final StreamCodec<ByteBuf, ServerboundSetPlayerAttackTargetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetPlayerAttackTargetPayload::entity,
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundSetPlayerAttackTargetPayload::target,
            ServerboundSetPlayerAttackTargetPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Entity e = player.level().getEntity(entity);
        Player p = player.level().getPlayerByUUID(target);
        if (e instanceof Mob mob)
            mob.setTarget(p);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
