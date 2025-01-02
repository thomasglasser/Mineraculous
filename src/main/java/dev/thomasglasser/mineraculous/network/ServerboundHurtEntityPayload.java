package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ServerboundHurtEntityPayload(int id, ResourceKey<DamageType> source, int amount) implements ExtendedPacketPayload {

    public static final Type<ServerboundHurtEntityPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_hurt_entity"));
    public static final StreamCodec<ByteBuf, ServerboundHurtEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundHurtEntityPayload::id,
            ResourceKey.streamCodec(Registries.DAMAGE_TYPE), ServerboundHurtEntityPayload::source,
            ByteBufCodecs.INT, ServerboundHurtEntityPayload::amount,
            ServerboundHurtEntityPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(id);
        if (entity != null) {
            entity.hurt(player.level().damageSources().source(source, player), amount);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
