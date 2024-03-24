package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ClientboundSyncMiraculousDataPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("sync_miraculous_data");

	private final MiraculousData data;
	private final int entity;

	public ClientboundSyncMiraculousDataPacket(MiraculousData data, int entity)
	{
		this.data = data;
		this.entity = entity;
	}

	public ClientboundSyncMiraculousDataPacket(FriendlyByteBuf buffer)
	{
		this.data = buffer.readWithCodecTrusted(NbtOps.INSTANCE, MiraculousData.CODEC);
		this.entity = buffer.readInt();
	}

	// ON CLIENT
	@Override
	public void handle(Player player)
	{
		if (player.level().getEntity(entity) instanceof LivingEntity livingEntity)
			Services.DATA.setMiraculousData(data, livingEntity, false);
	}

	@Override
	public Direction direction()
	{
		return Direction.SERVER_TO_CLIENT;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeWithCodec(NbtOps.INSTANCE, MiraculousData.CODEC, data);
		buffer.writeInt(entity);
	}

	public static FriendlyByteBuf write(MiraculousData data, int entity)
	{
		FriendlyByteBuf buffer = PacketUtils.create();
		buffer.writeWithCodec(NbtOps.INSTANCE, MiraculousData.CODEC, data);
		buffer.writeInt(entity);
		return buffer;
	}

	public static FriendlyByteBuf write(MiraculousData data, LivingEntity entity)
	{
		return write(data, entity.getId());
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
