package dev.thomasglasser.mineraculous.impl.mixin.minecraft.data.recipes;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.thomasglasser.mineraculous.impl.world.item.crafting.MineraculousRecipeSerializers;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SimpleCookingRecipeBuilder.class)
public class SimpleCookingRecipeBuilderMixin {
    @Expression("? != ?")
    @ModifyExpressionValue(method = "determineRecipeCategory", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean overrideAllowedFoodRecipes(boolean original, @Local(argsOnly = true) RecipeSerializer<?> serializer) {
        return original && serializer != MineraculousRecipeSerializers.OVEN.get();
    }
}
