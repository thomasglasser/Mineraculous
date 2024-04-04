package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ClientboundSyncMiraculousDataSetPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("sync_miraculous_data_set");

	private final MiraculousDataSet miraculousDataSet;
	private final int entity;

	public ClientboundSyncMiraculousDataSetPacket(MiraculousDataSet miraculousDataSet, int entity)
	{
		this.miraculousDataSet = miraculousDataSet;
		this.entity = entity;
	}

	public ClientboundSyncMiraculousDataSetPacket(FriendlyByteBuf buffer)
	{
		this.miraculousDataSet = buffer.readWithCodecTrusted(NbtOps.INSTANCE, MiraculousDataSet.CODEC);
		this.entity = buffer.readInt();
	}

	// ON CLIENT
	@Override
	public void handle(Player player)
	{
		if (player.level().getEntity(entity) instanceof LivingEntity livingEntity)
			Services.DATA.setMiraculousDataSet(livingEntity, miraculousDataSet, false);
	}

	@Override
	public Direction direction()
	{
		return Direction.SERVER_TO_CLIENT;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeWithCodec(NbtOps.INSTANCE, MiraculousDataSet.CODEC, miraculousDataSet);
		buffer.writeInt(entity);
	}

	public static FriendlyByteBuf write(MiraculousDataSet miraculousDataSet, int entity)
	{
		FriendlyByteBuf buffer = PacketUtils.create();
		buffer.writeWithCodec(NbtOps.INSTANCE, MiraculousDataSet.CODEC, miraculousDataSet);
		buffer.writeInt(entity);
		return buffer;
	}

	public static FriendlyByteBuf write(MiraculousDataSet miraculousDataSet, LivingEntity entity)
	{
		return write(miraculousDataSet, entity.getId());
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
