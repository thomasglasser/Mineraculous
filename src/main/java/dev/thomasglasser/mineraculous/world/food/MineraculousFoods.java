package dev.thomasglasser.mineraculous.world.food;

import net.minecraft.world.food.FoodProperties;

public class MineraculousFoods {
    public static final FoodProperties CHEESE = new FoodProperties.Builder().nutrition(2).build();
    public static final FoodProperties CAMEMBERT = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();
}
