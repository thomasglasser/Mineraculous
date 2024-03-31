package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import dev.thomasglasser.tommylib.api.world.entity.DataHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class ClientboundToggleCatVisionPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("clientbound_toggle_cat_vision");

	private final boolean catVision;

	public ClientboundToggleCatVisionPacket(boolean catVision)
	{
		this.catVision = catVision;
	}

	public ClientboundToggleCatVisionPacket(FriendlyByteBuf buffer)
	{
		this.catVision = buffer.readBoolean();
	}

	// ON CLIENT
	@Override
	public void handle(Player player)
	{
		if (catVision)
		{
			MineraculousClientUtils.setShader(MineraculousEntityEvents.CAT_VISION_SHADER);
			player.addEffect(MineraculousEntityEvents.INFINITE_HIDDEN_EFFECT.apply(MobEffects.NIGHT_VISION, 0));
		}
		else
		{
			MineraculousClientUtils.setShader(null);
			player.removeEffect(MobEffects.NIGHT_VISION);
		}
		((DataHolder) player).getPersistentData().putBoolean(MineraculousEntityEvents.TAG_HASCATVISION, catVision);
	}

	@Override
	public Direction direction()
	{
		return Direction.SERVER_TO_CLIENT;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeBoolean(catVision);
	}

	public static FriendlyByteBuf write(boolean greenVision)
	{
		FriendlyByteBuf buffer = PacketUtils.create();
		buffer.writeBoolean(greenVision);
		return buffer;
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
