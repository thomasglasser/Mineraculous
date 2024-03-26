package dev.thomasglasser.mineraculous.core;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.MineraculousPackets;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.curio.CatMiraculousItemCurio;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.MiraculousItemCurio;
import dev.thomasglasser.tommylib.api.network.NeoForgePacketUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class MineraculousNeoForgeCoreEvents
{
	public static void onRegisterPackets(RegisterPayloadHandlerEvent event)
	{
		IPayloadRegistrar registrar = event.registrar(Mineraculous.MOD_ID);
		MineraculousPackets.PACKETS.forEach((packet, pair) -> NeoForgePacketUtils.register(registrar, packet, pair));
	}

	public static void onFMLCommonSetup(FMLCommonSetupEvent event)
	{
		CuriosApi.registerCurio(MineraculousItems.CAT_MIRACULOUS.get(), new ICurioItem() {
			private final MiraculousItemCurio curio = new CatMiraculousItemCurio();

			@Override
			public void curioTick(SlotContext slotContext, ItemStack stack)
			{
				curio.tick(stack, new CuriosData(slotContext.index(), "", slotContext.identifier()), slotContext.entity());
			}

			@Override
			public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack)
			{
				curio.onEquip(stack, new CuriosData(slotContext.index(), "", slotContext.identifier()), slotContext.entity());
			}

			@Override
			public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack)
			{
				return true;
			}
		});
	}
}
