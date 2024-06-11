package dev.thomasglasser.mineraculous.platform.services;

import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface CuriosHelper
{
	void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack, boolean syncToClient);

	ItemStack getStackInSlot(LivingEntity entity, CuriosData data);

	Map<CuriosData, ItemStack> getAllItems(LivingEntity entity);

	default void openExternalInventory(Player target)
	{
		ClientUtils.setScreen(new ExternalInventoryScreen(target));
	}
}
