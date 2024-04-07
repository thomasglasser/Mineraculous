package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class ClientboundToggleCatVisionPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("clientbound_toggle_cat_vision");

	private final boolean catVision;
	
	public ClientboundToggleCatVisionPacket(FriendlyByteBuf buf)
	{
		this.catVision = buf.readBoolean();
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
		CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(player);
		tag.putBoolean(MineraculousEntityEvents.TAG_HASCATVISION, catVision);
		TommyLibServices.ENTITY.setPersistentData(player, tag, false);
	}

	@Override
	public Direction direction()
	{
		return Direction.SERVER_TO_CLIENT;
	}

	@Override
	public void write(FriendlyByteBuf buf)
	{
		buf.writeBoolean(catVision);
	}

	public static FriendlyByteBuf write(boolean greenVision)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeBoolean(greenVision);
		return buf;
	}

	@Override
	public ResourceLocation id()
	{
		return ID;
	}
}
