package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.Level;

public class MineraculousCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Mineraculous.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MIRACULOUS = TABS.register("miraculous", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(Mineraculous.modLoc("miraculous").toLanguageKey("item_group"))).icon(() -> {
        Level level = ClientUtils.getLevel();
        if (level != null) {
            return Miraculous.createMiraculousStack(level.holderOrThrow(Miraculouses.LADYBUG));
        }
        return ItemStack.EMPTY;
    }).withSearchBar().displayItems((parameters, output) -> generateMiraculous(output, parameters.holders().lookupOrThrow(MineraculousRegistries.MIRACULOUS))).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KAMIKOTIZATION_TOOLS = TABS.register("kamikotization_tools", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(Mineraculous.modLoc("kamikotization_tools").toLanguageKey("item_group"))).icon(MineraculousArmors.KAMIKOTIZATION.HEAD::toStack).displayItems((parameters, output) -> {
        ReferenceLinkedOpenHashSet<Kamikotization> kamikotizations = parameters.holders().lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION).listElements().sorted(Comparator.comparing(Holder.Reference::key)).map(Holder::value).collect(Collectors.toCollection(ReferenceLinkedOpenHashSet::new));
        for (Kamikotization kamikotization : kamikotizations) {
            if (kamikotization.powerSource().left().isPresent()) {
                output.accept(kamikotization.powerSource().left().get());
            }
        }
    }).withTabsBefore(MIRACULOUS.getKey()).build());

    private static final ItemPredicate ANY = ItemPredicate.Builder.item().build();
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KAMIKOTIZABLES = TABS.register("kamikotizables", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(Mineraculous.modLoc("kamikotizables").toLanguageKey("item_group"))).icon(MineraculousArmors.KAMIKOTIZATION.HEAD::toStack).displayItems((parameters, output) -> {
        Set<ItemStack> set = ItemStackLinkedSet.createTypeAndComponentsSet();

        for (CreativeModeTab creativemodetab : parameters.holders().lookupOrThrow(Registries.CREATIVE_MODE_TAB).listElements().map(Holder::value).toList()) {
            if (creativemodetab.getType() != CreativeModeTab.Type.SEARCH) {
                LinkedHashSet<Kamikotization> kamikotizations = parameters.holders().lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION).listElements().sorted(Comparator.comparing(Holder.Reference::key)).map(Holder::value).collect(Collectors.toCollection(LinkedHashSet::new));
                for (ItemStack stack : creativemodetab.getSearchTabDisplayItems()) {
                    if (kamikotizations.stream().anyMatch(kamikotization -> !kamikotization.itemPredicate().equals(ANY) && kamikotization.itemPredicate().test(stack))) {
                        set.add(stack);
                    }
                }
            }
        }

        output.acceptAll(set);
    }).withTabsBefore(KAMIKOTIZATION_TOOLS.getKey()).build());

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
    }).withTabsBefore(KAMIKOTIZABLES.getKey()).build());

    private static void generateMiraculous(
            CreativeModeTab.Output output,
            HolderLookup.RegistryLookup<Miraculous> miraculous) {
        miraculous.listElements()
                .forEach(m -> output.accept(Miraculous.createMiraculousStack(m)));
    }

    public static void init() {}
}
