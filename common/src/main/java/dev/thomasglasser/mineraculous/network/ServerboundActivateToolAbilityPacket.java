package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class ServerboundActivateToolAbilityPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("serverbound_activate_tool_ability");

	private final boolean travelling;
	private final InteractionHand hand;

	public ServerboundActivateToolAbilityPacket(FriendlyByteBuf buf)
	{
		this.travelling = buf.readBoolean();
		this.hand = buf.readEnum(InteractionHand.class);
	}

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		player.getItemInHand(hand).getOrCreateTag().putBoolean(CatStaffItem.TAG_TRAVELLING, travelling);
	}

	@Override
	public Direction direction()
	{
		return Direction.CLIENT_TO_SERVER;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(travelling);
		buf.writeEnum(hand);
	}

	public static FriendlyByteBuf write(boolean travelling, InteractionHand hand)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeBoolean(travelling);
		buf.writeEnum(hand);
		return buf;
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
