package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import java.util.List;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class MineraculousArmors {
    public static final DeferredRegister.Items ARMORS = DeferredRegister.createItems(Mineraculous.MOD_ID);
    public static final Item.Properties DEFAULT_PROPERTIES = new Item.Properties().stacksTo(1).rarity(Rarity.EPIC);

    public static final ArmorSet MIRACULOUS = createMiraculousSet();

    private static ArmorSet createMiraculousSet() {
        DeferredItem<ArmorItem> mask = ItemUtils.register(ARMORS, "miraculous_mask", () -> new MiraculousArmorItem(ArmorItem.Type.HELMET, DEFAULT_PROPERTIES), List.of());
        DeferredItem<ArmorItem> chestplate = ItemUtils.register(ARMORS, "miraculous_chestplate", () -> new MiraculousArmorItem(ArmorItem.Type.CHESTPLATE, DEFAULT_PROPERTIES), List.of());
        DeferredItem<ArmorItem> leggings = ItemUtils.register(ARMORS, "miraculous_leggings", () -> new MiraculousArmorItem(ArmorItem.Type.LEGGINGS, DEFAULT_PROPERTIES), List.of());
        DeferredItem<ArmorItem> boots = ItemUtils.register(ARMORS, "miraculous_boots", () -> new MiraculousArmorItem(ArmorItem.Type.BOOTS, DEFAULT_PROPERTIES), List.of());

        return new ArmorSet("miraculous", "Miraculous", mask, chestplate, leggings, boots);
    }

    public static void init() {}
}
