package dev.thomasglasser.mineraculous.world.item.curio;

import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.network.ServerboundMiraculousTransformPacket;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
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
		if (entity.level().isClientSide && entity instanceof Player player && stack.getItem() instanceof MiraculousItem miraculousItem && miraculousItem.getAcceptableSlots().contains(curiosData.name()))
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
				MiraculousData data = Services.DATA.getMiraculousData(player);
				if (MineraculousKeyMappings.TRANSFORM.isDown())
				{
					if (data.transformed())
					{
						TommyLibServices.NETWORK.sendToServer(ServerboundMiraculousTransformPacket.class, ServerboundMiraculousTransformPacket.write(stack, curiosData, false));
					}
					else
					{
						TommyLibServices.NETWORK.sendToServer(ServerboundMiraculousTransformPacket.class, ServerboundMiraculousTransformPacket.write(stack, curiosData, true));
					}
					playerData.putInt(waitTicksKey, 10);
				}
				else if (MineraculousKeyMappings.ACTIVATE_POWER.isDown() && data.transformed())
				{
					// TODO: Activate power packet
					playerData.putInt(waitTicksKey, 10);
				}
			}
		}
	}

	@Override
	public void onEquip(ItemStack stack, CuriosData curiosData, LivingEntity entity)
	{
		if (!entity.level().isClientSide && entity instanceof Player player)
		{
			if (stack.getOrCreateTag().getBoolean(MiraculousItem.TAG_POWERED) && !Services.DATA.getMiraculousData(entity).transformed())
			{
				((MiraculousItem)stack.getItem()).summonKwami(entity.level(), stack, curiosData, player);
				stack.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, false);
			}
		}
	}
}
