package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.tommylib.api.world.item.equipment.ExtendedArmorMaterial;
import java.util.EnumMap;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorType;

public interface MineraculousArmorMaterials {
    ExtendedArmorMaterial MIRACULOUS = new ExtendedArmorMaterial(0, Util.make(new EnumMap<>(ArmorType.class), defense -> {
        defense.put(ArmorType.BOOTS, 30);
        defense.put(ArmorType.LEGGINGS, 30);
        defense.put(ArmorType.CHESTPLATE, 30);
        defense.put(ArmorType.HELMET, 30);
        defense.put(ArmorType.BODY, 30);
    }), 0, SoundEvents.ARMOR_EQUIP_LEATHER, 20.0F, 0.0F);

    static void init() {}
}
