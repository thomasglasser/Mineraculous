package dev.thomasglasser.mineraculous.api.world.item.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.item.armor.KamikotizationArmorItem;
import dev.thomasglasser.mineraculous.impl.world.item.armor.MiraculousArmorItem;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousArmors {
    private static final DeferredRegister.Items ARMORS = DeferredRegister.createItems(Mineraculous.MOD_ID);

    public static final ArmorSet MIRACULOUS = createMiraculousSet();
    public static final ArmorSet KAMIKOTIZATION = createKamikotizationSet();

    private static ArmorSet createMiraculousSet() {
        DeferredItem<ArmorItem> mask = ItemUtils.register(ARMORS, "miraculous_mask", () -> new MiraculousArmorItem(ArmorItem.Type.HELMET, ArmorSet.DEFAULT_PROPERTIES.apply(new Item.Properties().rarity(Rarity.EPIC))));
        DeferredItem<ArmorItem> chestplate = ItemUtils.register(ARMORS, "miraculous_chestplate", () -> new MiraculousArmorItem(ArmorItem.Type.CHESTPLATE, ArmorSet.DEFAULT_PROPERTIES.apply(new Item.Properties().rarity(Rarity.EPIC))));
        DeferredItem<ArmorItem> leggings = ItemUtils.register(ARMORS, "miraculous_leggings", () -> new MiraculousArmorItem(ArmorItem.Type.LEGGINGS, ArmorSet.DEFAULT_PROPERTIES.apply(new Item.Properties().rarity(Rarity.EPIC))));
        DeferredItem<ArmorItem> boots = ItemUtils.register(ARMORS, "miraculous_boots", () -> new MiraculousArmorItem(ArmorItem.Type.BOOTS, ArmorSet.DEFAULT_PROPERTIES.apply(new Item.Properties().rarity(Rarity.EPIC))));

        return new ArmorSet("miraculous", mask, chestplate, leggings, boots);
    }

    private static ArmorSet createKamikotizationSet() {
        DeferredItem<ArmorItem> mask = ItemUtils.register(ARMORS, "kamikotization_mask", () -> new KamikotizationArmorItem(ArmorItem.Type.HELMET, ArmorSet.DEFAULT_PROPERTIES.apply(new Item.Properties().rarity(Rarity.EPIC))));
        DeferredItem<ArmorItem> chestplate = ItemUtils.register(ARMORS, "kamikotization_chestplate", () -> new KamikotizationArmorItem(ArmorItem.Type.CHESTPLATE, ArmorSet.DEFAULT_PROPERTIES.apply(new Item.Properties().rarity(Rarity.EPIC))));
        DeferredItem<ArmorItem> leggings = ItemUtils.register(ARMORS, "kamikotization_leggings", () -> new KamikotizationArmorItem(ArmorItem.Type.LEGGINGS, ArmorSet.DEFAULT_PROPERTIES.apply(new Item.Properties().rarity(Rarity.EPIC))));
        DeferredItem<ArmorItem> boots = ItemUtils.register(ARMORS, "kamikotization_boots", () -> new KamikotizationArmorItem(ArmorItem.Type.BOOTS, ArmorSet.DEFAULT_PROPERTIES.apply(new Item.Properties().rarity(Rarity.EPIC))));

        return new ArmorSet("kamikotization", mask, chestplate, leggings, boots);
    }

    @ApiStatus.Internal
    public static void init() {}
}
