package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ServerboundActivateMainPowerPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("serverbound_activate_main_power");

	private final MiraculousType type;

	public ServerboundActivateMainPowerPacket(FriendlyByteBuf buf)
	{
		this.type = buf.readEnum(MiraculousType.class);
	}

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(player);
		MiraculousData data = miraculousDataSet.get(type);
		data.miraculousItem().getOrCreateTag().putInt(MiraculousItem.TAG_REMAININGTICKS, MiraculousItem.FIVE_MINUTES);
		miraculousDataSet.put(player, type, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, true, data.name()), true);
		Services.CURIOS.setStackInSlot(player, data.curiosData(), data.miraculousItem(), true);
	}

	@Override
	public Direction direction()
	{
		return Direction.CLIENT_TO_SERVER;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeEnum(type);
	}

	public static FriendlyByteBuf write(MiraculousType type)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeEnum(type);
		return buf;
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
