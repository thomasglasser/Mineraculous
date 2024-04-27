package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundActivateMainPowerPayload(MiraculousType miraculousType) implements ExtendedPacketPayload
{
	public static final Type<ServerboundActivateMainPowerPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_activate_main_power"));
	public static final StreamCodec<FriendlyByteBuf, ServerboundActivateMainPowerPayload> CODEC = StreamCodec.composite(
			NetworkUtils.enumCodec(MiraculousType.class), ServerboundActivateMainPowerPayload::miraculousType,
			ServerboundActivateMainPowerPayload::new
	);

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(player);
		MiraculousData data = miraculousDataSet.get(miraculousType);
		data.miraculousItem().set(MineraculousDataComponents.REMAINING_TICKS.get(), MiraculousItem.FIVE_MINUTES);
		miraculousDataSet.put(player, miraculousType, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, true, data.name()), true);
		Services.CURIOS.setStackInSlot(player, data.curiosData(), data.miraculousItem(), true);
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
