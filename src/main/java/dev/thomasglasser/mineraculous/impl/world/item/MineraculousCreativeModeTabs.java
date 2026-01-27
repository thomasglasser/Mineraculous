package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousCreativeModeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MineraculousConstants.MOD_ID);

    /// Holds all data-driven {@link MiraculousItem}s.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MIRACULOUS = TABS.register("miraculous", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(MineraculousConstants.modLoc("miraculous").toLanguageKey("item_group"))).icon(() -> {
        Level level = ClientUtils.getLevel();
        if (level != null) {
            return Miraculous.createMiraculousStack(level.holderOrThrow(Miraculouses.LADYBUG));
        }
        return ItemStack.EMPTY;
    }).withSearchBar().displayItems((parameters, output) -> parameters.holders().lookupOrThrow(MineraculousRegistries.MIRACULOUS).listElements()
            .forEach(miraculous -> output.accept(Miraculous.createMiraculousStack(miraculous)))).build());

    /// Holds all tools specified in data-driven {@link Kamikotization}s.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KAMIKOTIZATION_TOOLS = TABS.register("kamikotization_tools", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(MineraculousConstants.modLoc("kamikotization_tools").toLanguageKey("item_group"))).icon(MineraculousArmors.KAMIKOTIZATION.head()::toStack).displayItems((parameters, output) -> {
        parameters.holders().lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION).listElements()
                .sorted(Comparator.comparing(Holder.Reference::key))
                .map(kamikotization -> kamikotization.value().powerSource().left())
                .forEach(powerSource -> powerSource.ifPresent(output::accept));
    }).withTabsBefore(MIRACULOUS.getKey()).build());

    private static final ItemPredicate ANY = ItemPredicate.Builder.item().build();
    /// Holds all items valid for predicated data-driven {@link Kamikotization}s.
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KAMIKOTIZABLES = TABS.register("kamikotizables", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(MineraculousConstants.modLoc("kamikotizables").toLanguageKey("item_group"))).icon(MineraculousArmors.KAMIKOTIZATION.head()::toStack).displayItems((parameters, output) -> {
        Set<ItemStack> set = ItemStackLinkedSet.createTypeAndComponentsSet();

        parameters.holders().lookupOrThrow(Registries.CREATIVE_MODE_TAB).listElements().map(Holder::value).forEach(tab -> {
            if (tab.getType() != CreativeModeTab.Type.SEARCH) {
                LinkedHashSet<Kamikotization> kamikotizations = parameters.holders().lookupOrThrow(MineraculousRegistries.KAMIKOTIZATION).listElements().sorted(Comparator.comparing(Holder.Reference::key)).map(Holder::value).collect(Collectors.toCollection(LinkedHashSet::new));
                for (ItemStack stack : tab.getSearchTabDisplayItems()) {
                    for (Kamikotization kamikotization : kamikotizations) {
                        if (!kamikotization.itemPredicate().equals(ANY) && kamikotization.itemPredicate().test(stack)) {
                            set.add(stack);
                            break;
                        }
                    }
                }
            }
        });

        output.acceptAll(set);
    }).withTabsBefore(KAMIKOTIZATION_TOOLS.getKey()).build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FAKE_MIRACULOUS = TABS.register("fake_miraculous", () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(MineraculousConstants.modLoc("fake_miraculous").toLanguageKey("item_group")))
            .icon(() -> {
                Level level = ClientUtils.getLevel();
                if (level != null) {
                    return Miraculous.createFakeMiraculousStack(level.holderOrThrow(Miraculouses.LADYBUG));
                }
                return ItemStack.EMPTY;
            }).displayItems((parameters, output) -> {
                parameters.holders().lookupOrThrow(MineraculousRegistries.MIRACULOUS).listElements()
                        .forEach(miraculous -> output.accept(Miraculous.createFakeMiraculousStack(miraculous)));
            }).withSearchBar().withTabsBefore(KAMIKOTIZABLES.getKey()).build());

    /// Holds all items from the mod
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MINERACULOUS = TABS.register(MineraculousConstants.MOD_ID, () -> TommyLibServices.CLIENT.tabBuilder().title(Component.translatable(MineraculousConstants.modLoc(MineraculousConstants.MOD_ID).toLanguageKey("item_group"))).icon(() -> MineraculousItems.CATACLYSM_DUST.get().getDefaultInstance()).type(CreativeModeTab.Type.SEARCH).displayItems((parameters, output) -> {
        Set<ItemStack> set = ItemStackLinkedSet.createTypeAndComponentsSet();

        parameters.holders().lookupOrThrow(Registries.CREATIVE_MODE_TAB).listElements().map(Holder::value).forEach(tab -> {
            if (tab.getType() != CreativeModeTab.Type.SEARCH) {
                for (ItemStack stack : tab.getSearchTabDisplayItems()) {
                    if (BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(MineraculousConstants.MOD_ID)) {
                        set.add(stack);
                    }
                }
            }
        });
        output.acceptAll(set);
    }).withTabsBefore(FAKE_MIRACULOUS.getKey()).build());

    @ApiStatus.Internal
    public static void init() {}
}
