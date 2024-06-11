package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.UUID;

public record ServerboundStealItemPayload(UUID target, int slot) implements ExtendedPacketPayload
{
	public static final Type<ServerboundStealItemPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_steal_item"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStealItemPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundStealItemPayload::target,
			ByteBufCodecs.INT, ServerboundStealItemPayload::slot,
			ServerboundStealItemPayload::new
	);

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		Player target = player.level().getPlayerByUUID(this.target);
		if (target != null)
		{
			ItemStack stack = target.inventoryMenu.slots.get(this.slot).getItem();
			if (stack.getItem() instanceof MiraculousItem miraculousItem)
			{
				MiraculousData miraculousData = Services.DATA.getMiraculousDataSet(target).get(miraculousItem.getType());
				if (miraculousData.miraculousItem() == stack)
				{
					Entity entity = ((ServerLevel) player.level()).getEntity(stack.get(MineraculousDataComponents.KWAMI_DATA.get()).uuid());
					if (entity instanceof Kwami kwami)
						MineraculousEntityEvents.renounceMiraculous(stack, kwami);
				}
			}
			if (EnchantmentHelper.hasBindingCurse(stack))
			{
				player.displayClientMessage(Component.translatable(ExternalInventoryScreen.ITEM_BOUND_KEY), true);
			}
			else
			{
				player.setItemInHand(InteractionHand.MAIN_HAND, stack);
				target.getInventory().removeItem(stack);
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
