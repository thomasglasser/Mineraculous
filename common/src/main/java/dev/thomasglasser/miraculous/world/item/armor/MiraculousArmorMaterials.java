package dev.thomasglasser.miraculous.world.item.armor;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

public enum MiraculousArmorMaterials implements StringRepresentable, ArmorMaterial
{
	MIRACULOUS("miraculous", 0, Util.make(new EnumMap<>(ArmorItem.Type.class), (enumMap) -> {
		enumMap.put(ArmorItem.Type.BOOTS, 2);
		enumMap.put(ArmorItem.Type.LEGGINGS, 5);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 6);
		enumMap.put(ArmorItem.Type.HELMET, 2);
	}), 9, SoundEvents.ARMOR_EQUIP_LEATHER /*TODO:Custom sound?*/, 0.0F, 0.0F);

	public static final Codec<MiraculousArmorMaterials> CODEC = StringRepresentable.fromEnum(MiraculousArmorMaterials::values);
	private static final EnumMap<ArmorItem.Type, Integer> HEALTH_FUNCTION_FOR_TYPE = Util.make(new EnumMap<>(ArmorItem.Type.class), (enumMap) -> {
		enumMap.put(ArmorItem.Type.BOOTS, 13);
		enumMap.put(ArmorItem.Type.LEGGINGS, 15);
		enumMap.put(ArmorItem.Type.CHESTPLATE, 16);
		enumMap.put(ArmorItem.Type.HELMET, 11);
	});
	private final String name;
	private final int durabilityMultiplier;
	private final EnumMap<ArmorItem.Type, Integer> protectionFunctionForType;
	private final int enchantmentValue;
	private final SoundEvent sound;
	private final float toughness;
	private final float knockbackResistance;

	MiraculousArmorMaterials(String name, int durabilityMultiplier, EnumMap protectionFunctionForType, int enchantmentValue, SoundEvent sound, float toughness, float knockbackResistance) {
		this.name = name;
		this.durabilityMultiplier = durabilityMultiplier;
		this.protectionFunctionForType = protectionFunctionForType;
		this.enchantmentValue = enchantmentValue;
		this.sound = sound;
		this.toughness = toughness;
		this.knockbackResistance = knockbackResistance;
	}

	public int getDurabilityForType(ArmorItem.Type type) {
		return HEALTH_FUNCTION_FOR_TYPE.get(type) * this.durabilityMultiplier;
	}

	public int getDefenseForType(ArmorItem.Type type) {
		return this.protectionFunctionForType.get(type);
	}

	public int getEnchantmentValue() {
		return this.enchantmentValue;
	}

	public SoundEvent getEquipSound() {
		return this.sound;
	}

	public Ingredient getRepairIngredient() {
		return Ingredient.EMPTY;
	}

	public String getName() {
		return this.name;
	}

	public float getToughness() {
		return this.toughness;
	}

	public float getKnockbackResistance() {
		return this.knockbackResistance;
	}

	public String getSerializedName() {
		return this.name;
	}
}
