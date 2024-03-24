package dev.thomasglasser.mineraculous.platform;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.thomasglasser.mineraculous.network.ClientboundSyncCurioPacket;
import dev.thomasglasser.mineraculous.platform.services.CuriosHelper;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class FabricCuriosHelper implements CuriosHelper
{
	@Override
	public void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack, boolean syncToClient)
	{
		TrinketsApi.getTrinketComponent(entity).orElseThrow().getInventory().get(curiosData.category()).get(curiosData.name()).setItem(curiosData.slot(), stack);
		if (syncToClient) TommyLibServices.NETWORK.sendToAllClients(ClientboundSyncCurioPacket.class, ClientboundSyncCurioPacket.write(entity, curiosData, stack), entity.level().getServer());
	}

	@Override
	public ItemStack getStackInSlot(LivingEntity entity, CuriosData curiosData)
	{
		return TrinketsApi.getTrinketComponent(entity).orElseThrow().getInventory().get(curiosData.category()).get(curiosData.name()).getItem(curiosData.slot());
	}

	@Override
	@Nullable
	public CuriosData getCuriosData(LivingEntity entity, ItemStack stack)
	{
		TrinketInventory inventory = TrinketsApi.getTrinketComponent(entity).orElseThrow().getEquipped(stack::equals).get(0).getA().inventory();
		for (int i = 0; i < inventory.getSlotType().getAmount(); i++)
		{
			if (inventory.getItem(i).equals(stack)) return new CuriosData(i, inventory.getSlotType().getGroup(), inventory.getSlotType().getName());
		}
		return null;
	}
}
