package dev.thomasglasser.mineraculous.world.item.curio;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.network.ClientboundToggleCatVisionPacket;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CatMiraculousItemCurio extends MiraculousItemCurio
{
	private boolean greenVision = false;

	@Override
	public void tick(ItemStack curio, CuriosData curiosData, LivingEntity entity)
	{
		super.tick(curio, curiosData, entity);
		MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(entity);
		MiraculousData catMiraculousData = miraculousDataSet.get(MiraculousType.CAT);
		if (!entity.level().isClientSide)
		{
			ItemStack mainHandItem = entity.getMainHandItem();
			if (catMiraculousData.powerActive() && !mainHandItem.isEmpty() && !mainHandItem.is(MineraculousItemTags.CATACLYSM_IMMUNE))
			{
				entity.setItemInHand(InteractionHand.MAIN_HAND, MineraculousEntityEvents.convertToCataclysmDust(mainHandItem));
				miraculousDataSet.put(entity, MiraculousType.CAT, new MiraculousData(catMiraculousData.transformed(), catMiraculousData.miraculousItem(), catMiraculousData.curiosData(), catMiraculousData.tool(), catMiraculousData.powerLevel(), true, false, catMiraculousData.name()), true);
			}
			if (entity instanceof ServerPlayer serverPlayer)
			{
				checkGreenVision(serverPlayer);
			}
		}
		else
		{
			if (catMiraculousData.powerActive() && !entity.isSpectator())
			{
				double randomShiftForward = 1.0 / entity.level().random.nextInt(8, 15);
				double randomShiftRight = 1.0 / entity.level().random.nextInt(8, 15);
				double randomShiftUp = 1.0 / entity.level().random.nextInt(15, 50);
				if (entity.level().random.nextBoolean())
					randomShiftForward = -randomShiftForward;
				if (entity.level().random.nextBoolean())
					randomShiftRight = -randomShiftRight;

				if (MineraculousClientUtils.isFirstPerson())
				{
					MineraculousClientUtils.renderParticlesFollowingEntity(entity, MineraculousParticleTypes.CATACLYSM.get(), 0.3, 0.1 + randomShiftForward, 0.23 + randomShiftRight, -0.1 - randomShiftUp, 0.4F, true);
				}
				else
				{
					MineraculousClientUtils.renderParticlesFollowingEntity(entity, MineraculousParticleTypes.CATACLYSM.get(), 0, 0.1 + randomShiftForward, 0.35 + randomShiftRight, 0.7 + randomShiftUp, 0.6F, false);
				}
			}
		}
	}

	@Override
	public void onUnequip(ItemStack oldStack, ItemStack newStack, CuriosData curiosData, LivingEntity entity)
	{
		if (greenVision && entity instanceof ServerPlayer serverPlayer)
		{
			checkGreenVision(serverPlayer);
		}
	}

	private void checkGreenVision(ServerPlayer serverPlayer)
	{
		MiraculousData miraculousData = Services.DATA.getMiraculousDataSet(serverPlayer).get(MiraculousType.CAT);
		if (serverPlayer.isSpectator())
		{
			if (greenVision)
			{
				greenVision = false;
				TommyLibServices.NETWORK.sendToClient(ClientboundToggleCatVisionPacket.class, ClientboundToggleCatVisionPacket.write(false), serverPlayer);
			}
		}
		else if (!miraculousData.transformed())
		{
			if (greenVision)
			{
				greenVision = false;
				TommyLibServices.NETWORK.sendToClient(ClientboundToggleCatVisionPacket.class, ClientboundToggleCatVisionPacket.write(false), serverPlayer);
			}
		}
		else if (serverPlayer.level().getMaxLocalRawBrightness(serverPlayer.blockPosition()) < 5)
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
