package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.network.ClientboundToggleCatVisionPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

public class CatMiraculousItem extends MiraculousItem
{
	private boolean greenVision = false;

	public CatMiraculousItem(Properties properties)
	{
		super(properties, MiraculousType.CAT, MineraculousArmors.CAT_MIRACULOUS, MineraculousItems.CAT_STAFF, null /* TODO: Transform sound */, MineraculousEntityTypes.PLAGG::get, "ring", TextColor.fromRgb(0xc6f800));
	}

	/*@Override
	public void curioTick(SlotContext slotContext, ItemStack stack)
	{
		super.curioTick(slotContext, stack);
		LivingEntity entity = slotContext.entity();
		MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
		MiraculousData catMiraculousData = miraculousDataSet.get(MiraculousType.CAT);
		if (!entity.level().isClientSide)
		{
			ItemStack mainHandItem = entity.getMainHandItem();
			if (catMiraculousData.mainPowerActive() && !mainHandItem.isEmpty() && !mainHandItem.is(MineraculousItemTags.CATACLYSM_IMMUNE))
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
			if (catMiraculousData.mainPowerActive() && !entity.isSpectator())
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
	public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack)
	{
		super.onUnequip(slotContext, newStack, stack);
		if (greenVision && slotContext.entity() instanceof ServerPlayer serverPlayer)
		{
			checkGreenVision(serverPlayer);
		}
	}*/

	private void checkGreenVision(ServerPlayer serverPlayer)
	{
		MiraculousData miraculousData = serverPlayer.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(MiraculousType.CAT);
		if (serverPlayer.isSpectator())
		{
			if (greenVision)
			{
				greenVision = false;
				TommyLibServices.NETWORK.sendToClient(new ClientboundToggleCatVisionPayload(false), serverPlayer);
			}
		}
		else if (!miraculousData.transformed())
		{
			if (greenVision)
			{
				greenVision = false;
				TommyLibServices.NETWORK.sendToClient(new ClientboundToggleCatVisionPayload(false), serverPlayer);
			}
		}
		else if (serverPlayer.level().getMaxLocalRawBrightness(serverPlayer.blockPosition().above()) < 5)
		{
			if (!greenVision)
			{
				greenVision = true;
				TommyLibServices.NETWORK.sendToClient(new ClientboundToggleCatVisionPayload(true), serverPlayer);
			}
		}
		else if (greenVision)
		{
			greenVision = false;
			TommyLibServices.NETWORK.sendToClient(new ClientboundToggleCatVisionPayload(false), serverPlayer);
		}
	}
}
