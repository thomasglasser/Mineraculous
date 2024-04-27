package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public record ServerboundActivateToolPayload(boolean activate, ItemStack stack, InteractionHand hand) implements ExtendedPacketPayload
{
	public static final Type<ServerboundActivateToolPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_activate_tool"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundActivateToolPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, ServerboundActivateToolPayload::activate,
			ItemStack.STREAM_CODEC, ServerboundActivateToolPayload::stack,
			NetworkUtils.enumCodec(InteractionHand.class), ServerboundActivateToolPayload::hand,
			ServerboundActivateToolPayload::new
	);

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		player.getItemInHand(hand).set(MineraculousDataComponents.POWERED.get(), activate);
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
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
