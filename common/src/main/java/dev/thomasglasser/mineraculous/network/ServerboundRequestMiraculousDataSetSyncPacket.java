package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ServerboundRequestMiraculousDataSetSyncPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("request_miraculous_data_set_sync");

	private final int entity;

	public ServerboundRequestMiraculousDataSetSyncPacket(FriendlyByteBuf buf)
	{
		this.entity = buf.readInt();
	}

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		if (player.level().getEntity(entity) instanceof LivingEntity livingEntity)
			TommyLibServices.NETWORK.sendToTrackingClients(ID, ClientboundSyncMiraculousDataSetPacket::new, ClientboundSyncMiraculousDataSetPacket.write(Services.DATA.getMiraculousDataSet(livingEntity), entity), player.level().getServer(), livingEntity);
	}

	@Override
	public Direction direction()
	{
		return Direction.CLIENT_TO_SERVER;
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeInt(entity);
	}

	public static FriendlyByteBuf write(int entity)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeInt(entity);
		return buf;
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
