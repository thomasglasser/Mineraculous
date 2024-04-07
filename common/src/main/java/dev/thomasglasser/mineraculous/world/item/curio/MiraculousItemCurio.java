package dev.thomasglasser.mineraculous.world.item.curio;

import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.network.ServerboundActivateMainPowerPacket;
import dev.thomasglasser.mineraculous.network.ServerboundMiraculousTransformPacket;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MiraculousItemCurio implements Curio
{
	@Override
	public void tick(ItemStack stack, CuriosData curiosData, LivingEntity entity)
	{
		if (entity instanceof Player player && stack.getItem() instanceof MiraculousItem miraculousItem && miraculousItem.getAcceptableSlot().getSecond().equals(curiosData.name()) && (curiosData.category().isEmpty() || miraculousItem.getAcceptableSlot().getFirst().equals(curiosData.category())))
		{
			MiraculousType miraculousType = miraculousItem.getType();
			MiraculousData data = Services.DATA.getMiraculousDataSet(player).get(miraculousType);
			if (data.mainPowerActivated())
				stack.getOrCreateTag().putInt(MiraculousItem.TAG_REMAININGTICKS, stack.getOrCreateTag().getInt(MiraculousItem.TAG_REMAININGTICKS) - 1);
			if (entity.level().isClientSide)
			{
				CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
				int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
				if (waitTicks <= 0 && !MineraculousClientUtils.hasScreenOpen())
				{
					if (MineraculousKeyMappings.OPEN_POWER_WHEEL.isDown() && data.transformed())
					{
						MineraculousClientEvents.openPowerWheel(player);
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
					else if (MineraculousKeyMappings.TRANSFORM.isDown())
					{
						if (data.transformed())
						{
							TommyLibServices.NETWORK.sendToServer(ServerboundMiraculousTransformPacket.ID, ServerboundMiraculousTransformPacket::new, ServerboundMiraculousTransformPacket.write(miraculousType, data, false));
						}
						else
						{
							TommyLibServices.NETWORK.sendToServer(ServerboundMiraculousTransformPacket.ID, ServerboundMiraculousTransformPacket::new, ServerboundMiraculousTransformPacket.write(miraculousType, data, true));
						}
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
					else if (MineraculousKeyMappings.ACTIVATE_MAIN_POWER.isDown() && data.transformed() && !data.mainPowerActive() && !data.mainPowerActivated())
					{
						TommyLibServices.NETWORK.sendToServer(ServerboundActivateMainPowerPacket.ID, ServerboundActivateMainPowerPacket::new, ServerboundActivateMainPowerPacket.write(miraculousType));
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
				}
				TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
			}
			else
			{
				if (data.mainPowerActivated() && stack.getOrCreateTag().getInt(MiraculousItem.TAG_REMAININGTICKS) <= 0)
				{
					MineraculousEntityEvents.handleTransformation(player, miraculousType, data, false);
				}
			}
		}

		stack.inventoryTick(entity.level(), entity, -1, false);
	}

	@Override
	public void onEquip(ItemStack stack, CuriosData curiosData, LivingEntity entity)
	{
		if (!entity.level().isClientSide && entity instanceof Player player && stack.getItem() instanceof MiraculousItem miraculousItem)
		{
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(entity);
			MiraculousData data = miraculousDataSet.get(miraculousItem.getType());
			if (stack.getOrCreateTag().getBoolean(MiraculousItem.TAG_POWERED) && !data.transformed())
			{
				stack.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, false);
				data = new MiraculousData(false, stack, curiosData, data.tool(), data.powerLevel(), data.mainPowerActivated(), data.mainPowerActive(), data.name());
				MineraculousEntityEvents.summonKwami(entity.level(), miraculousItem.getType(), data, player);
			}
		}
	}

	@Override
	public void onUnequip(ItemStack oldStack, ItemStack newStack, CuriosData curiosData, LivingEntity entity)
	{
		if (oldStack.getItem() instanceof MiraculousItem miraculousItem)
		{
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(entity);
			MiraculousData miraculousData = miraculousDataSet.get(miraculousItem.getType());
			miraculousDataSet.put(entity, miraculousItem.getType(), new MiraculousData(miraculousData.transformed(), miraculousData.miraculousItem(), new CuriosData(), miraculousData.tool(), miraculousData.powerLevel(), miraculousData.mainPowerActivated(), miraculousData.mainPowerActive(), miraculousData.name()), true);
		}
	}
}
