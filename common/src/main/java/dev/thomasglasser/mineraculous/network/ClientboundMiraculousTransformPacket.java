package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ClientboundMiraculousTransformPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("clientbound_miraculous_transform");

	private ItemStack miraculous;
	private CuriosData curiosData;
	private boolean transform;
	private ItemStack tool;

	public ClientboundMiraculousTransformPacket(ItemStack miraculous, CuriosData curiosData, boolean transform, ItemStack tool)
	{
		this.miraculous = miraculous;
		this.curiosData = curiosData;
		this.transform = transform;
		this.tool = tool;
	}

	public ClientboundMiraculousTransformPacket(FriendlyByteBuf buf)
	{
		miraculous = buf.readItem();
		curiosData = buf.readWithCodecTrusted(NbtOps.INSTANCE, CuriosData.CODEC);
		transform = buf.readBoolean();
		tool = buf.readItem();
	}

	// ON CLIENT
	@Override
	public void handle(Player player)
	{
		Services.DATA.setMiraculousData(new MiraculousData(transform, tool), player);
		if (transform)
		{
			// TODO: Start player anim
			miraculous.getOrCreateTag().putInt(MiraculousItem.TAG_TRANSFORMTICKS, 0 /* TODO: Set it to however long the transform anim is */);
			miraculous.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, true);
			Services.CURIOS.setStackInSlot(player, curiosData, miraculous);
		}
		else
		{
			// TODO: Start player anim
			miraculous.getOrCreateTag().putInt(MiraculousItem.TAG_DETRANSFORMTICKS, 0 /* TODO: Set it to however long the detransform anim is */);
			miraculous.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, false);
			Services.CURIOS.setStackInSlot(player, curiosData, miraculous);
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
		buffer.writeItem(miraculous);
		buffer.writeWithCodec(NbtOps.INSTANCE, CuriosData.CODEC, curiosData);
		buffer.writeBoolean(transform);
		buffer.writeItem(tool);
	}

	public static FriendlyByteBuf write(ItemStack miraculous, CuriosData curiosData, boolean transform, ItemStack tool)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeItem(miraculous);
		buf.writeWithCodec(NbtOps.INSTANCE, CuriosData.CODEC, curiosData);
		buf.writeBoolean(transform);
		buf.writeItem(tool);
		return buf;
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
