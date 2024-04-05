package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import dev.thomasglasser.tommylib.api.world.entity.DataHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ClientboundLivingEntityCataclysmedPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("clientbound_living_entity_cataclysmed");

	private final int entity;

	public ClientboundLivingEntityCataclysmedPacket(FriendlyByteBuf buf)
	{
		entity = buf.readInt();
	}

	// ON CLIENT
	@Override
	public void handle(Player player)
	{
		Entity target = player.level().getEntity(entity);
		if (target != null)
			((DataHolder) target).getPersistentData().putBoolean(MineraculousEntityEvents.TAG_CATACLYSMED, true);
	}

	@Override
	public Direction direction()
	{
		return Direction.SERVER_TO_CLIENT;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeInt(entity);
	}

	public static FriendlyByteBuf write(LivingEntity entity)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeInt(entity.getId());
		return buf;
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
