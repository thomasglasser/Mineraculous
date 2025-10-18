package dev.thomasglasser.mineraculous.api.world.inventory;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.world.inventory.OvenMenu;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class MineraculousMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<OvenMenu>> OVEN = register("oven", OvenMenu::new);

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> register(String name, MenuType.MenuSupplier<T> factory, FeatureFlagSet flags) {
        return MENU_TYPES.register(name, () -> new MenuType<>(factory, flags));
    }

    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> register(String name, MenuType.MenuSupplier<T> factory) {
        return register(name, factory, FeatureFlags.VANILLA_SET);
    }

    public static void init() {}
}
