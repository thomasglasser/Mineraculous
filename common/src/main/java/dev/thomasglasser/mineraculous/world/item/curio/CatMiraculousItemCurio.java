package dev.thomasglasser.mineraculous.world.item.curio;

import dev.thomasglasser.mineraculous.network.ClientboundToggleCatVisionPacket;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CatMiraculousItemCurio extends MiraculousItemCurio
{
	private boolean greenVision = false;

	@Override
	public void tick(ItemStack stack, CuriosData curiosData, LivingEntity entity)
	{
		super.tick(stack, curiosData, entity);
		MiraculousData miraculousData = Services.DATA.getMiraculousData(entity);
		if (!entity.level().isClientSide)
		{
			if (miraculousData.powerActive())
			{
				if (!entity.getMainHandItem().isEmpty())
				{
					entity.setItemInHand(InteractionHand.MAIN_HAND, MineraculousEntityEvents.convertToMiraculousDust(entity.getMainHandItem()));
					Services.DATA.setMiraculousData(new MiraculousData(miraculousData.transformed(), miraculousData.miraculous(), miraculousData.curiosData(), miraculousData.tool(), miraculousData.powerLevel(), true, false, miraculousData.name()), entity, true);
				}
			}
			if (entity instanceof ServerPlayer serverPlayer)
			{
				if (!miraculousData.transformed() && greenVision)
				{
					greenVision = false;
					TommyLibServices.NETWORK.sendToClient(ClientboundToggleCatVisionPacket.class, ClientboundToggleCatVisionPacket.write(false), serverPlayer);
				}
				else if (entity.level().getMaxLocalRawBrightness(serverPlayer.blockPosition()) < 8)
				{
					if (!greenVision)
					{
						greenVision = true;
						TommyLibServices.NETWORK.sendToClient(ClientboundToggleCatVisionPacket.class, ClientboundToggleCatVisionPacket.write(true), serverPlayer);
					}
				}
				else if (greenVision)
				{
					greenVision = false;
					TommyLibServices.NETWORK.sendToClient(ClientboundToggleCatVisionPacket.class, ClientboundToggleCatVisionPacket.write(false), serverPlayer);
				}
			}
		}
	}
}
