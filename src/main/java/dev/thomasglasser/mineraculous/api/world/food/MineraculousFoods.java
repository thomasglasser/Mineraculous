package dev.thomasglasser.mineraculous.api.world.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class MineraculousFoods {
    /// Base food properties of a cheese wedge
    public static final FoodProperties CHEESE = new FoodProperties.Builder().nutrition(2).build();
    /// Food properties of a camembert wedge, like cheese but slightly saturating
    public static final FoodProperties CAMEMBERT = new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build();

    public static final FoodProperties RAW_MACARON = new FoodProperties.Builder().nutrition(1).effect(() -> new MobEffectInstance(MobEffects.HUNGER, 300, 0), 0.15F).build();
    public static final FoodProperties MACARON = new FoodProperties.Builder().nutrition(4).saturationModifier(0.2F).build();
}
