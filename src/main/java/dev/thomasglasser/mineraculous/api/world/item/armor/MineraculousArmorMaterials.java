package dev.thomasglasser.mineraculous.api.world.item.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousArmorMaterials {
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, Mineraculous.MOD_ID);

    /// Magical armor, strongest level but unenchantable and unrepairable
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MIRACULOUS = register("miraculous", Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 30);
        map.put(ArmorItem.Type.LEGGINGS, 30);
        map.put(ArmorItem.Type.CHESTPLATE, 30);
        map.put(ArmorItem.Type.HELMET, 30);
        map.put(ArmorItem.Type.BODY, 30);
    }), 0, Holder.direct(SoundEvents.EMPTY), 20.0F, 0.0F, () -> Ingredient.EMPTY);

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(
            String name,
            EnumMap<ArmorItem.Type, Integer> defense,
            int enchantmentValue,
            Holder<SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient) {
        List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(Mineraculous.modLoc(name)));

        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(defense, enchantmentValue, equipSound, repairIngredient, list, toughness, knockbackResistance));
    }

    @ApiStatus.Internal
    public static void init() {}
}
