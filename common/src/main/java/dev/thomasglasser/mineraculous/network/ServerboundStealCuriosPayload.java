package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
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

public record ServerboundStealCuriosPayload(UUID target, CuriosData data) implements ExtendedPacketPayload
{
	public static final Type<ServerboundStealCuriosPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_steal_curios"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStealCuriosPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundStealCuriosPayload::target,
			CuriosData.STREAM_CODEC, ServerboundStealCuriosPayload::data,
			ServerboundStealCuriosPayload::new
	);

	// ON SERVER
	@Override
	public void handle(Player player)
	{
		Player target = player.level().getPlayerByUUID(this.target);
		if (target != null)
		{
			ItemStack stack = Services.CURIOS.getStackInSlot(target, this.data);
			if (stack.getItem() instanceof MiraculousItem miraculousItem)
			{
				MiraculousData miraculousData = Services.DATA.getMiraculousDataSet(target).get(miraculousItem.getType());
				if (miraculousData.miraculousItem() == stack)
				{
					if (miraculousData.transformed())
						MineraculousEntityEvents.handleTransformation(target, miraculousItem.getType(), miraculousData, false);
					Entity entity = ((ServerLevel) player.level()).getEntity(stack.get(MineraculousDataComponents.KWAMI_DATA.get()).uuid());
					if (entity instanceof Kwami kwami)
						MineraculousEntityEvents.renounceMiraculous(stack, kwami);
					stack = Services.CURIOS.getStackInSlot(target, this.data);
				}
			}
			if (EnchantmentHelper.hasBindingCurse(stack))
			{
				player.displayClientMessage(Component.translatable(ExternalInventoryScreen.ITEM_BOUND_KEY), true);
			}
			else
			{
				player.setItemInHand(InteractionHand.MAIN_HAND, stack);
				Services.CURIOS.setStackInSlot(target, this.data, ItemStack.EMPTY, true);
			}
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}
}
