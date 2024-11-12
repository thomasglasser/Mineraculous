package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;

public class MineraculousArmors {
    public static final DeferredRegister.Items ARMORS = DeferredRegister.createItems(Mineraculous.MOD_ID);
    public static final UnaryOperator<Item.Properties> DEFAULT_PROPERTIES = properties -> properties.stacksTo(1);

    public static final ArmorSet MIRACULOUS = createMiraculousSet();
    public static final ArmorSet KAMIKOTIZATION = createKamikotizedSet();

    private static ArmorSet createMiraculousSet() {
        DeferredItem<ArmorItem> mask = ItemUtils.register(ARMORS, "miraculous_mask", properties -> new MiraculousArmorItem(ArmorType.HELMET, DEFAULT_PROPERTIES.apply(properties.rarity(Rarity.EPIC))), List.of());
        DeferredItem<ArmorItem> chestplate = ItemUtils.register(ARMORS, "miraculous_chestplate", properties -> new MiraculousArmorItem(ArmorType.CHESTPLATE, DEFAULT_PROPERTIES.apply(properties.rarity(Rarity.EPIC))), List.of());
        DeferredItem<ArmorItem> leggings = ItemUtils.register(ARMORS, "miraculous_leggings", properties -> new MiraculousArmorItem(ArmorType.LEGGINGS, DEFAULT_PROPERTIES.apply(properties.rarity(Rarity.EPIC))), List.of());
        DeferredItem<ArmorItem> boots = ItemUtils.register(ARMORS, "miraculous_boots", properties -> new MiraculousArmorItem(ArmorType.BOOTS, DEFAULT_PROPERTIES.apply(properties.rarity(Rarity.EPIC))), List.of());

        return new ArmorSet("miraculous", "Miraculous", mask, chestplate, leggings, boots);
    }

    private static ArmorSet createKamikotizedSet() {
        DeferredItem<ArmorItem> mask = ItemUtils.register(ARMORS, "kamikotized_mask", properties -> new KamikotizationArmorItem(ArmorType.HELMET, DEFAULT_PROPERTIES.apply(properties.rarity(Rarity.EPIC))), List.of());
        DeferredItem<ArmorItem> chestplate = ItemUtils.register(ARMORS, "kamikotized_chestplate", properties -> new KamikotizationArmorItem(ArmorType.CHESTPLATE, DEFAULT_PROPERTIES.apply(properties.rarity(Rarity.EPIC))), List.of());
        DeferredItem<ArmorItem> leggings = ItemUtils.register(ARMORS, "kamikotized_leggings", properties -> new KamikotizationArmorItem(ArmorType.LEGGINGS, DEFAULT_PROPERTIES.apply(properties.rarity(Rarity.EPIC))), List.of());
        DeferredItem<ArmorItem> boots = ItemUtils.register(ARMORS, "kamikotized_boots", properties -> new KamikotizationArmorItem(ArmorType.BOOTS, DEFAULT_PROPERTIES.apply(properties.rarity(Rarity.EPIC))), List.of());

        return new ArmorSet("kamikotized", "Kamikotized", mask, chestplate, leggings, boots);
    }

    public static void init() {}
}
