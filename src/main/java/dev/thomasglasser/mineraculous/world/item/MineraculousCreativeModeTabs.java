package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class MineraculousCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Mineraculous.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MINERACULOUS = TABS.register(Mineraculous.MOD_ID, () -> TommyLibServices.ITEM.newTab(Component.translatable(Mineraculous.modLoc(Mineraculous.MOD_ID).toLanguageKey("item_group")), () -> MineraculousItems.CATACLYSM_DUST.get().getDefaultInstance(), true, (parameters, output) -> {
        List<ResourceLocation> itemsToAdd = new ArrayList<>();

        ItemUtils.getItemTabs().forEach((tab, rls) -> rls.forEach(rl -> {
            if (rl.getNamespace().equals(Mineraculous.MOD_ID) && !(tab == MineraculousCreativeModeTabs.MINERACULOUS.getKey()) && !itemsToAdd.contains(rl))
                itemsToAdd.add(rl);
        }));

        output.acceptAll(itemsToAdd.stream().map(rl -> BuiltInRegistries.ITEM.get(rl).getDefaultInstance()).toList());
    }));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MIRACULOUS = TABS.register("miraculous", () -> TommyLibServices.ITEM.newTab(Component.translatable(Mineraculous.modLoc("miraculous").toLanguageKey("item_group")), () -> {
        ItemStack stack = MineraculousItems.MIRACULOUS.get().getDefaultInstance();
        stack.set(MineraculousDataComponents.MIRACULOUS, MineraculousMiraculous.LADYBUG);
        return stack;
    }, false, (parameters, output) -> output.acceptAll(MineraculousMiraculous.getMiraculousForAll(parameters.holders()))));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MIRACULOUS_SUITS = TABS.register("miraculous_suits", () -> TommyLibServices.ITEM.newTab(Component.translatable(Mineraculous.modLoc("miraculous_suits").toLanguageKey("item_group")), () -> {
        ItemStack stack = MineraculousArmors.MIRACULOUS.HEAD.toStack();
        stack.set(MineraculousDataComponents.MIRACULOUS, MineraculousMiraculous.LADYBUG);
        return stack;
    }, false, (parameters, output) -> output.acceptAll(MineraculousMiraculous.getArmorForAll(parameters.holders()))));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KAMIKOTIZATION_SUITS = TABS.register("kamikotization_suits", () -> TommyLibServices.ITEM.newTab(Component.translatable(Mineraculous.modLoc("kamikotization_suits").toLanguageKey("item_group")), () -> {
        ItemStack stack = MineraculousItems.MIRACULOUS.toStack();
        stack.set(MineraculousDataComponents.MIRACULOUS, MineraculousMiraculous.BUTTERFLY);
        // TODO: Replace with kamikotized armor texture
        return stack;
    }, false, (parameters, output) -> output.acceptAll(Kamikotization.getArmorForAll(parameters.holders()))));

    public static void init() {}
}
