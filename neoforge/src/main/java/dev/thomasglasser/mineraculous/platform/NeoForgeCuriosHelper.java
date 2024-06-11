package dev.thomasglasser.mineraculous.platform;

import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.network.ClientboundSyncCurioPayload;
import dev.thomasglasser.mineraculous.platform.services.CuriosHelper;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.HashMap;
import java.util.Map;

public class NeoForgeCuriosHelper implements CuriosHelper
{
	@Override
	public void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack, boolean syncToClient)
	{
		CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(curiosData.name()).getStacks().setStackInSlot(curiosData.slot(), stack);
		if (syncToClient) TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncCurioPayload(entity.getId(), curiosData, stack), entity.level().getServer());
	}

	@Override
	public ItemStack getStackInSlot(LivingEntity entity, CuriosData curiosData)
	{
		return CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(curiosData.name()).getStacks().getStackInSlot(curiosData.slot());
	}

	@Override
	public Map<CuriosData, ItemStack> getAllItems(LivingEntity entity)
	{
		Map<CuriosData, ItemStack> items = new HashMap<>();
		ICuriosItemHandler curios = CuriosApi.getCuriosInventory(entity).orElseThrow();
		curios.getCurios().forEach((name, handler) -> {
			for (int i = 0; i < handler.getStacks().getSlots(); i++)
			{
				ItemStack stack = handler.getStacks().getStackInSlot(i);
				if (!stack.isEmpty()) items.put(new CuriosData(i, "", name), stack);
			}
		});
		return items;
	}

	@Override
	public void openExternalInventory(Player target)
	{
		ClientUtils.setScreen(new ExternalCuriosInventoryScreen(target));
	}
}
