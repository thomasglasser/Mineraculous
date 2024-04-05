package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClientboundSyncCurioPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("clientbound_sync_curios");

	private final int entity;
	private final CuriosData curiosData;
	private final ItemStack stack;

	public ClientboundSyncCurioPacket(FriendlyByteBuf buffer)
	{
		entity = buffer.readInt();
		curiosData = buffer.readWithCodecTrusted(NbtOps.INSTANCE, CuriosData.CODEC);
		stack = buffer.readItem();
	}

	// ON CLIENT
	@Override
	public void handle(Player player)
	{
		Entity found = player.level().getEntity(entity);
		if (found instanceof LivingEntity livingEntity)
			Services.CURIOS.setStackInSlot(livingEntity, curiosData, stack, false);
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
		buffer.writeWithCodec(NbtOps.INSTANCE, CuriosData.CODEC, curiosData);
		buffer.writeItem(stack);
	}

	public static FriendlyByteBuf write(int entity, CuriosData curiosData, ItemStack stack)
	{
		FriendlyByteBuf buffer = PacketUtils.create();
		buffer.writeInt(entity);
		buffer.writeWithCodec(NbtOps.INSTANCE, CuriosData.CODEC, curiosData);
		buffer.writeItem(stack);
		return buffer;
	}

	public static FriendlyByteBuf write(LivingEntity entity, CuriosData curiosData, ItemStack stack)
	{
		return write(entity.getId(), curiosData, stack);
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
