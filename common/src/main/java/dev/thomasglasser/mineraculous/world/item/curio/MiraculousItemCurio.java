package dev.thomasglasser.mineraculous.world.item.curio;

import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.network.ServerboundActivatePowerPacket;
import dev.thomasglasser.mineraculous.network.ServerboundMiraculousTransformPacket;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.DataHolder;
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
			if (data.powerActivated())
				stack.getOrCreateTag().putInt(MiraculousItem.TAG_REMAININGTICKS, stack.getOrCreateTag().getInt(MiraculousItem.TAG_REMAININGTICKS) - 1);
			if (entity.level().isClientSide)
			{
				String waitTicksKey = "WaitTicks";
				CompoundTag playerData = ((DataHolder) player).getPersistentData();
				int waitTicks = playerData.getInt(waitTicksKey);
				if (waitTicks > 0)
				{
					playerData.putInt(waitTicksKey, --waitTicks);
				}
				else
				{
					if (MineraculousKeyMappings.TRANSFORM.isDown())
					{
						if (data.transformed())
						{
							TommyLibServices.NETWORK.sendToServer(ServerboundMiraculousTransformPacket.class, ServerboundMiraculousTransformPacket.write(miraculousType, data, false));
						}
						else
						{
							TommyLibServices.NETWORK.sendToServer(ServerboundMiraculousTransformPacket.class, ServerboundMiraculousTransformPacket.write(miraculousType, data, true));
						}
						playerData.putInt(waitTicksKey, 10);
					}
					else if (MineraculousKeyMappings.ACTIVATE_POWER.isDown() && data.transformed() && !data.powerActive() && !data.powerActivated())
					{
						TommyLibServices.NETWORK.sendToServer(ServerboundActivatePowerPacket.class, ServerboundActivatePowerPacket.write(miraculousType));
						playerData.putInt(waitTicksKey, 10);
					}
				}
			}
			else
			{
				if (data.powerActivated() && stack.getOrCreateTag().getInt(MiraculousItem.TAG_REMAININGTICKS) <= 0)
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
				data = new MiraculousData(false, stack, curiosData, data.tool(), data.powerLevel(), data.powerActivated(), data.powerActive(), data.name());
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
			miraculousDataSet.put(entity, miraculousItem.getType(), new MiraculousData(miraculousData.transformed(), miraculousData.miraculousItem(), new CuriosData(), miraculousData.tool(), miraculousData.powerLevel(), miraculousData.powerActivated(), miraculousData.powerActive(), miraculousData.name()), true);
		}
	}
}
