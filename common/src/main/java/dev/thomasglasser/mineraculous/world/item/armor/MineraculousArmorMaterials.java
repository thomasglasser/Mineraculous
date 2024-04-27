package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class MineraculousArmorMaterials
{
	public static final RegistrationProvider<ArmorMaterial> ARMOR_MATERIALS = RegistrationProvider.get(Registries.ARMOR_MATERIAL, Mineraculous.MOD_ID);

	public static final RegistryObject<ArmorMaterial> MIRACULOUS = register("miraculous", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323379_ -> {
		p_323379_.put(ArmorItem.Type.BOOTS, 30);
		p_323379_.put(ArmorItem.Type.LEGGINGS, 30);
		p_323379_.put(ArmorItem.Type.CHESTPLATE, 30);
		p_323379_.put(ArmorItem.Type.HELMET, 30);
		p_323379_.put(ArmorItem.Type.BODY, 30);
	}), 0, SoundEvents.ARMOR_EQUIP_LEATHER, 20.0F, 0.0F, () -> Ingredient.EMPTY);

	private static RegistryObject<ArmorMaterial> register(
			String name,
			EnumMap<ArmorItem.Type, Integer> defense,
			int enchantmentValue,
			Holder<SoundEvent> equipSound,
			float toughness,
			float knockbackResistance,
			Supplier<Ingredient> repairIngredient
	) {
		EnumMap<ArmorItem.Type, Integer> enummap = new EnumMap<>(ArmorItem.Type.class);

		for (ArmorItem.Type armoritem$type : ArmorItem.Type.values()) {
			enummap.put(armoritem$type, defense.get(armoritem$type));
		}

		List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(new ResourceLocation(name)));

		return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(enummap, enchantmentValue, equipSound, repairIngredient, list, toughness, knockbackResistance));
	}

	public static void init() {}
}
