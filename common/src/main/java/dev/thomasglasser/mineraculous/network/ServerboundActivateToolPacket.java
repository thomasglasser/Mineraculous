package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public class ServerboundActivateToolPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("serverbound_activate_tool");

	private final boolean activate;
	private final ItemStack stack;
	private final InteractionHand hand;

	public ServerboundActivateToolPacket(FriendlyByteBuf buf)
	{
		this.activate = buf.readBoolean();
		this.stack = buf.readWithCodecTrusted(NbtOps.INSTANCE, ItemStack.CODEC);
		this.hand = buf.readEnum(InteractionHand.class);
	}

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		player.getItemInHand(hand).getOrCreateTag().putBoolean(CatStaffItem.TAG_ACTIVATED, activate);
		if (activate)
		{
			((SingletonGeoAnimatable)stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) player.level()), "use_controller", "extend");
		}
		else
		{
			((SingletonGeoAnimatable)stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, (ServerLevel) player.level()), "use_controller", "retract");
		}
	}

	@Override
	public Direction direction()
	{
		return Direction.CLIENT_TO_SERVER;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeBoolean(activate);
		buf.writeWithCodec(NbtOps.INSTANCE, ItemStack.CODEC, stack);
		buf.writeEnum(hand);
	}

	public static FriendlyByteBuf write(boolean activate, ItemStack stack, InteractionHand hand)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeBoolean(activate);
		buf.writeWithCodec(NbtOps.INSTANCE, ItemStack.CODEC, stack);
		buf.writeEnum(hand);
		return buf;
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
