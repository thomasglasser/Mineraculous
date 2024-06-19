package dev.thomasglasser.mineraculous.world.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public enum MineraculousTiers implements Tier
{
	MIRACULOUS(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, -1, 18.0F, 8.0F, -1, () -> Ingredient.EMPTY);

	private final TagKey<Block> incorrectBlocksForDrops;
	private final int uses;
	private final float speed;
	private final float damage;
	private final int enchantmentValue;
	private final Supplier<Ingredient> repairIngredient;

	MineraculousTiers(final TagKey<Block> incorrectBlocksForDrops, final int uses, final float speed, final float damage, final int enchantmentValue, final Supplier<Ingredient> repairIngredient) {
		this.incorrectBlocksForDrops = incorrectBlocksForDrops;
		this.uses = uses;
		this.speed = speed;
		this.damage = damage;
		this.enchantmentValue = enchantmentValue;
		this.repairIngredient = repairIngredient;
	}

	public int getUses() {
		return this.uses;
	}

	public float getSpeed() {
		return this.speed;
	}

	public float getAttackDamageBonus() {
		return this.damage;
	}

	public TagKey<Block> getIncorrectBlocksForDrops() {
		return this.incorrectBlocksForDrops;
	}

	public int getEnchantmentValue() {
		return this.enchantmentValue;
	}

	public Ingredient getRepairIngredient() {
		return (Ingredient)this.repairIngredient.get();
	}
}
