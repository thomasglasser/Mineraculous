package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;

public record ServerboundSpawnTamedKamikoPayload(UUID ownerId, BlockPos spawnPos) implements ExtendedPacketPayload {
    public static final Type<ServerboundSpawnTamedKamikoPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_spawn_tamed_kamiko"));
    public static final StreamCodec<ByteBuf, ServerboundSpawnTamedKamikoPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundSpawnTamedKamikoPayload::ownerId,
            BlockPos.STREAM_CODEC, ServerboundSpawnTamedKamikoPayload::spawnPos,
            ServerboundSpawnTamedKamikoPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().spawn((ServerLevel) player.level(), spawnPos, MobSpawnType.CONVERSION);
        if (kamiko != null)
            kamiko.setOwnerUUID(ownerId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
