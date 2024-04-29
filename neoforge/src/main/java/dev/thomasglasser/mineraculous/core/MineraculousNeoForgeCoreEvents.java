package dev.thomasglasser.mineraculous.core;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.curio.CatMiraculousItemCurio;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.MiraculousItemCurio;
import dev.thomasglasser.tommylib.api.network.NeoForgeNetworkUtils;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class MineraculousNeoForgeCoreEvents
{
	public static void onRegisterPackets(RegisterPayloadHandlersEvent event)
	{
		PayloadRegistrar registrar = event.registrar(Mineraculous.MOD_ID);
		MineraculousPayloads.PAYLOADS.forEach((info) -> NeoForgeNetworkUtils.register(registrar, info));
	}

	public static void onFMLCommonSetup(FMLCommonSetupEvent event)
	{
		// TODO: Update Curios
//		CuriosApi.registerCurio(MineraculousItems.CAT_MIRACULOUS.get(), new ICurioItem() {
//			private final MiraculousItemCurio curio = new CatMiraculousItemCurio();
//
//			@Override
//			public void curioTick(SlotContext slotContext, ItemStack stack)
//			{
//				curio.tick(stack, new CuriosData(slotContext.index(), "", slotContext.identifier()), slotContext.entity());
//			}
//
//			@Override
//			public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack)
//			{
//				curio.onEquip(stack, new CuriosData(slotContext.index(), "", slotContext.identifier()), slotContext.entity());
//			}
//
//			@Override
//			public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack)
//			{
//				curio.onUnequip(stack, newStack, new CuriosData(slotContext.index(), "", slotContext.identifier()), slotContext.entity());
//			}
//
//			@Override
//			public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack)
//			{
//				return true;
//			}
//		});
	}
}
