package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public record ServerboundActivateToolAbilityPayload(boolean traveling, InteractionHand hand) implements ExtendedPacketPayload
{
	public static final Type<ServerboundActivateToolAbilityPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_activate_tool_ability"));
	public static final StreamCodec<FriendlyByteBuf, ServerboundActivateToolAbilityPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, ServerboundActivateToolAbilityPayload::traveling,
			NetworkUtils.enumCodec(InteractionHand.class), ServerboundActivateToolAbilityPayload::hand,
			ServerboundActivateToolAbilityPayload::new
	);

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		player.getItemInHand(hand).set(MineraculousDataComponents.TRAVELING.get(), traveling);
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
