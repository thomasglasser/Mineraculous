package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerboundMiraculousTransformPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("serverbound_miraculous_transform");

	private final MiraculousType type;
	private final MiraculousData data;
	private final boolean transform;

	public ServerboundMiraculousTransformPacket(MiraculousType type, MiraculousData data, boolean transform)
	{
		this.type = type;
		this.data = data;
		this.transform = transform;
	}

	public ServerboundMiraculousTransformPacket(FriendlyByteBuf buf)
	{
		type = buf.readEnum(MiraculousType.class);
		data = buf.readWithCodecTrusted(NbtOps.INSTANCE, MiraculousData.CODEC);
		transform = buf.readBoolean();
	}

	// ON SERVER
	@Override
	public void handle(@Nullable Player player)
	{
		MineraculousEntityEvents.handleTransformation(player, type, data, transform);
	}

	@Override
	public Direction direction()
	{
		return Direction.CLIENT_TO_SERVER;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeEnum(type);
		buffer.writeWithCodec(NbtOps.INSTANCE, MiraculousData.CODEC, data);
		buffer.writeBoolean(transform);
	}

	public static FriendlyByteBuf write(MiraculousType type, MiraculousData data, boolean transform)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeEnum(type);
		buf.writeWithCodec(NbtOps.INSTANCE, MiraculousData.CODEC, data);
		buf.writeBoolean(transform);
		return buf;
	}

	@Override
	public @NotNull ResourceLocation id()
	{
		return ID;
	}
}
