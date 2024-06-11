package dev.thomasglasser.mineraculous.world.item.curio;

import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundActivateMainPowerPayload;
import dev.thomasglasser.mineraculous.network.ServerboundMiraculousTransformPayload;
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
				stack.set(MineraculousDataComponents.REMAINING_TICKS.get(), stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) - 1);
			if (entity.level().isClientSide)
			{
				CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
				int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
				if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen())
				{
					if (MineraculousKeyMappings.OPEN_ABILITY_WHEEL.isDown() && data.transformed())
					{
						MineraculousClientEvents.openPowerWheel(player);
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
					else if (MineraculousKeyMappings.TRANSFORM.isDown())
					{
						if (data.transformed())
						{
							TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculousType, data, false));
						}
						else
						{
							TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculousType, data, true));
						}
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
					else if (MineraculousKeyMappings.ACTIVATE_MAIN_POWER.isDown() && data.transformed() && !data.mainPowerActive() && !data.mainPowerActivated())
					{
						TommyLibServices.NETWORK.sendToServer(new ServerboundActivateMainPowerPayload(miraculousType));
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
				}
				TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
			}
			else
			{
				if (data.mainPowerActivated() && stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) <= 0)
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
			if (stack.has(MineraculousDataComponents.POWERED.get()) && !data.transformed())
			{
				stack.remove(MineraculousDataComponents.POWERED.get());
				data = new MiraculousData(false, stack, curiosData, data.tool(), data.powerLevel(), data.mainPowerActivated(), data.mainPowerActive(), data.name());
				MineraculousEntityEvents.summonKwami(entity.level(), miraculousItem.getType(), data, player);
			}
		}
	}
}
