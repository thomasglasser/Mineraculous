package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetAttackTargetPayload(int entity, int target) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetAttackTargetPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_attack_target"));
    public static final StreamCodec<ByteBuf, ServerboundSetAttackTargetPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetAttackTargetPayload::entity,
            ByteBufCodecs.INT, ServerboundSetAttackTargetPayload::target,
            ServerboundSetAttackTargetPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Entity e = player.level().getEntity(entity);
        Entity t = player.level().getEntity(target);
        if (e instanceof Mob mob && t instanceof LivingEntity livingEntity)
            mob.setTarget(livingEntity);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
