package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerboundMiraculousTransformPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("serverbound_miraculous_transform");

	private final ItemStack miraculous;
	private final CuriosData curiosData;
	private final boolean transform;

	public ServerboundMiraculousTransformPacket(ItemStack miraculous, CuriosData curiosData, boolean transform)
	{
		this.miraculous = miraculous;
		this.curiosData = curiosData;
		this.transform = transform;
	}

	public ServerboundMiraculousTransformPacket(FriendlyByteBuf buf)
	{
		miraculous = buf.readItem();
		curiosData = buf.readWithCodecTrusted(NbtOps.INSTANCE, CuriosData.CODEC);
		transform = buf.readBoolean();
	}

	// ON SERVER
	@Override
	public void handle(@Nullable Player player)
	{
		MineraculousEntityEvents.handleTransformation(player, miraculous, curiosData, transform);
	}

	@Override
	public Direction direction()
	{
		return Direction.CLIENT_TO_SERVER;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeItem(miraculous);
		buffer.writeWithCodec(NbtOps.INSTANCE, CuriosData.CODEC, curiosData);
		buffer.writeBoolean(transform);
	}

	public static FriendlyByteBuf write(ItemStack miraculous, CuriosData curiosData, boolean transform)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeItem(miraculous);
		buf.writeWithCodec(NbtOps.INSTANCE, CuriosData.CODEC, curiosData);
		buf.writeBoolean(transform);
		return buf;
	}

	@Override
	public @NotNull ResourceLocation id()
	{
		return ID;
	}
}
