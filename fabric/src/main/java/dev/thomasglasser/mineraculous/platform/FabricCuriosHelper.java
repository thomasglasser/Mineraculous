package dev.thomasglasser.mineraculous.platform;

import dev.emi.trinkets.api.TrinketsApi;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalTrinketsInventoryScreen;
import dev.thomasglasser.mineraculous.network.ClientboundSyncCurioPayload;
import dev.thomasglasser.mineraculous.platform.services.CuriosHelper;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class FabricCuriosHelper implements CuriosHelper
{
	@Override
	public void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack, boolean syncToClient)
	{
		TrinketsApi.getTrinketComponent(entity).orElseThrow().getInventory().get(curiosData.category()).get(curiosData.name()).setItem(curiosData.slot(), stack);
		if (syncToClient) TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncCurioPayload(entity.getId(), curiosData, stack), entity.level().getServer());
	}

	@Override
	public ItemStack getStackInSlot(LivingEntity entity, CuriosData curiosData)
	{
		return TrinketsApi.getTrinketComponent(entity).orElseThrow().getInventory().get(curiosData.category()).get(curiosData.name()).getItem(curiosData.slot());
	}

	@Override
	public Map<CuriosData, ItemStack> getAllItems(LivingEntity entity)
	{
		Map<CuriosData, ItemStack> items = new HashMap<>();
		TrinketsApi.getTrinketComponent(entity).orElseThrow().getInventory().forEach((group, inGroup) -> inGroup.forEach((name, inv) ->
		{
			for (int i = 0; i < inv.getContainerSize(); i++)
			{
				ItemStack stack = inv.getItem(i);
				if (!stack.isEmpty())
					items.put(new CuriosData(i, group, name), stack);
			}
		}));
		return items;
	}

	@Override
	public void openExternalInventory(Player target)
	{
		ClientUtils.setScreen(new ExternalTrinketsInventoryScreen(target));
	}
}