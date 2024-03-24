package dev.thomasglasser.mineraculous.platform.services;

import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface CuriosHelper
{
	void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack, boolean syncToClient);

	ItemStack getStackInSlot(LivingEntity entity, CuriosData data);

	@Nullable
	CuriosData getCuriosData(LivingEntity entity, ItemStack stack);
}
