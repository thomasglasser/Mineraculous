package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record ServerboundMiraculousTransformPayload(MiraculousType miraculousType, MiraculousData data, boolean transform) implements ExtendedPacketPayload
{
	public static final Type<ServerboundMiraculousTransformPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_miraculous_transform"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMiraculousTransformPayload> CODEC = StreamCodec.composite(
			NetworkUtils.enumCodec(MiraculousType.class), ServerboundMiraculousTransformPayload::miraculousType,
			MiraculousData.STREAM_CODEC, ServerboundMiraculousTransformPayload::data,
			ByteBufCodecs.BOOL, ServerboundMiraculousTransformPayload::transform,
			ServerboundMiraculousTransformPayload::new
	);

	// ON SERVER
	@Override
	public void handle(@Nullable Player player)
	{
		MineraculousEntityEvents.handleTransformation(player, miraculousType, data, transform);
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
