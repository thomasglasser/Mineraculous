package dev.thomasglasser.mineraculous.client.gui.screens.inventory;

import dev.emi.trinkets.TrinketScreen;
import dev.emi.trinkets.TrinketScreenManager;
import dev.emi.trinkets.TrinketSlot;
import dev.emi.trinkets.api.TrinketsApi;
import dev.thomasglasser.mineraculous.mixin.trinkets.api.TrinketInventoryAccessor;
import dev.thomasglasser.mineraculous.network.ServerboundStealCuriosPayload;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public class ExternalTrinketsInventoryScreen extends ExternalInventoryScreen
{
	public ExternalTrinketsInventoryScreen(Player player)
	{
		super(player);
	}

	@Override
	protected void init()
	{
		super.init();
		TrinketScreenManager.init((TrinketScreen) this);
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type)
	{
		if (slot instanceof TrinketSlot trinketSlot)
		{
			if (slot.hasItem() && mouseButton == 0)
			{
				if (type == ClickType.PICKUP)
				{
					TommyLibServices.NETWORK.sendToServer(new ServerboundStealCuriosPayload(target.getUUID(), new CuriosData(((TrinketInventoryAccessor) TrinketsApi.getTrinketComponent(target).orElseThrow().getInventory().get(trinketSlot.getType().getGroup()).get(trinketSlot.getType().getName())).getStacks().indexOf(slot.getItem()), trinketSlot.getType().getGroup(), trinketSlot.getType().getName())));
					ClientUtils.setScreen(null);
				}
			}
		}
		else
		{
			super.slotClicked(slot, slotId, mouseButton, type);
		}
	}
}
