package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ClientboundMiraculousTransformPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("clientbound_miraculous_transform");

	private MiraculousType type;
	private MiraculousData data;

	public ClientboundMiraculousTransformPacket(MiraculousType type, MiraculousData data)
	{
		this.type = type;
		this.data = data;
	}

	public ClientboundMiraculousTransformPacket(FriendlyByteBuf buf)
	{
		type = buf.readEnum(MiraculousType.class);
		data = buf.readWithCodecTrusted(NbtOps.INSTANCE, MiraculousData.CODEC);
	}

	// ON CLIENT
	@Override
	public void handle(Player player)
	{
		if (data.transformed())
		{
			// TODO: Start player anim
			data.miraculousItem().getOrCreateTag().putInt(MiraculousItem.TAG_TRANSFORMINGTICKS, 0 /* TODO: Set it to however long the transform anim is */);
			Services.CURIOS.setStackInSlot(player, data.curiosData(), data.miraculousItem(), false);
			if (data.name().isEmpty())
				player.displayClientMessage(Component.translatable(MiraculousData.NAME_NOT_SET, Component.translatable(type.getTranslationKey()), type.getSerializedName()), true);
		}
		else
		{
			// TODO: Start player anim
			data.miraculousItem().getOrCreateTag().putInt(MiraculousItem.TAG_DETRANSFORMINGTICKS, 0 /* TODO: Set it to however long the detransform anim is */);
			Services.CURIOS.setStackInSlot(player, data.curiosData(), data.miraculousItem(), false);
		}
	}

	@Override
	public Direction direction()
	{
		return Direction.SERVER_TO_CLIENT;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeEnum(type);
		buffer.writeWithCodec(NbtOps.INSTANCE, MiraculousData.CODEC, data);
	}

	public static FriendlyByteBuf write(MiraculousType type, MiraculousData data)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeEnum(type);
		buf.writeWithCodec(NbtOps.INSTANCE, MiraculousData.CODEC, data);
		return buf;
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
