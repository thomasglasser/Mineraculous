package dev.thomasglasser.mineraculous.platform;

import dev.thomasglasser.mineraculous.network.ClientboundSyncCurioPacket;
import dev.thomasglasser.mineraculous.platform.services.CuriosHelper;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class NeoForgeCuriosHelper implements CuriosHelper
{
	@Override
	public void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack, boolean syncToClient)
	{
		CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(curiosData.name()).getStacks().setStackInSlot(curiosData.slot(), stack);
		if (syncToClient) TommyLibServices.NETWORK.sendToAllClients(ClientboundSyncCurioPacket.class, ClientboundSyncCurioPacket.write(entity, curiosData, stack), entity.level().getServer());
	}

	@Override
	public ItemStack getStackInSlot(LivingEntity entity, CuriosData curiosData)
	{
		return CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(curiosData.name()).getStacks().getStackInSlot(curiosData.slot());
	}
}
