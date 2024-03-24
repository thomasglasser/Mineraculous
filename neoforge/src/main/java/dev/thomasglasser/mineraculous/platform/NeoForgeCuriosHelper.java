package dev.thomasglasser.mineraculous.platform;

import dev.thomasglasser.mineraculous.platform.services.CuriosHelper;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class NeoForgeCuriosHelper implements CuriosHelper
{
	@Override
	public void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack)
	{
		CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(curiosData.name()).getStacks().setStackInSlot(curiosData.slot(), stack);
	}

	@Override
	public ItemStack getStackInSlot(LivingEntity entity, CuriosData curiosData)
	{
		return CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(curiosData.name()).getStacks().getStackInSlot(curiosData.slot());
	}
}
