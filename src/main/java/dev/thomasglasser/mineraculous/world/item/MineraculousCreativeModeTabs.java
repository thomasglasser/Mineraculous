package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

public class MineraculousCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Mineraculous.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MIRACULOUS = TABS.register("miraculous", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(Mineraculous.modLoc("miraculous").toLanguageKey("item_group"))).icon(() -> Miraculous.createItemStack(MineraculousItems.MIRACULOUS.get(), MineraculousMiraculous.LADYBUG)).displayItems((parameters, output) -> {
        generateMiraculous(MineraculousItems.MIRACULOUS.get(), output, parameters.holders().lookupOrThrow(MineraculousRegistries.MIRACULOUS));
    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MIRACULOUS_SUITS = TABS.register("miraculous_suits", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(Mineraculous.modLoc("miraculous_suits").toLanguageKey("item_group"))).icon(() -> Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.HEAD.get(), MineraculousMiraculous.LADYBUG)).displayItems((parameters, output) -> {
        generateMiraculousArmor(MineraculousArmors.MIRACULOUS, output, parameters.holders().lookupOrThrow(MineraculousRegistries.MIRACULOUS));
    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KAMIKOTIZATION_SUITS = TABS.register("kamikotization_suits", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(Mineraculous.modLoc("kamikotization_suits").toLanguageKey("item_group"))).icon(() -> {
        // TODO: Replace with kamikotized armor texture
        return Miraculous.createItemStack(MineraculousItems.MIRACULOUS.get(), MineraculousMiraculous.BUTTERFLY);
    }).displayItems((parameters, output) -> {
        generateKamikotizedSet(MineraculousArmors.KAMIKOTIZATION, output, parameters.holders().lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION));
    }).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MINERACULOUS = TABS.register(Mineraculous.MOD_ID, () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(Mineraculous.modLoc(Mineraculous.MOD_ID).toLanguageKey("item_group"))).icon(() -> MineraculousItems.CATACLYSM_DUST.get().getDefaultInstance()).type(CreativeModeTab.Type.SEARCH).displayItems((parameters, output) -> {
        Set<ItemStack> set = ItemStackLinkedSet.createTypeAndComponentsSet();

        for (CreativeModeTab creativemodetab : parameters.holders().lookupOrThrow(Registries.CREATIVE_MODE_TAB).listElements().map(Holder::value).toList()) {
            if (creativemodetab.getType() != CreativeModeTab.Type.SEARCH) {
                for (ItemStack stack : creativemodetab.getSearchTabDisplayItems()) {
                    if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(Mineraculous.MOD_ID)) {
                        set.add(stack);
                    }
                }
            }
        }

        output.acceptAll(set);
    }).build());

    private static void generateMiraculous(
            Item item,
            CreativeModeTab.Output output,
            HolderLookup.RegistryLookup<Miraculous> miraculous) {
        miraculous.listElements()
                .forEach(m -> output.accept(Miraculous.createItemStack(item, m.key())));
    }

    private static void generateMiraculousArmor(
            ArmorSet armorSet,
            CreativeModeTab.Output output,
            HolderLookup.RegistryLookup<Miraculous> miraculous) {
        miraculous.listElements()
                .forEach(
                        m -> {
                            armorSet.getAllAsItems().forEach(item -> {
                                output.accept(Miraculous.createItemStack(item, m.key()));
                            });
                        });
    }

    private static void generateKamikotizedSet(
            ArmorSet armorSet,
            CreativeModeTab.Output output,
            HolderLookup.RegistryLookup<Kamikotization> kamikotizations) {
        kamikotizations.listElements()
                .forEach(
                        kamikotization -> {
                            armorSet.getAllAsItems().forEach(item -> {
                                output.accept(Kamikotization.createItemStack(item, kamikotization.key()));
                            });
                        });
    }

    public static void init() {}
}
