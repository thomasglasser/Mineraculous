package dev.thomasglasser.mineraculous.api.world.item.crafting.transmute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

/// A {@link RecipeSerializer} for building a {@link AbstractCookingRecipe} for transmuting.
public class SimpleTransmuteCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
    private final Factory<T> factory;
    private final MapCodec<T> codec;
    private final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;

    public SimpleTransmuteCookingSerializer(Factory<T> factory, int cookingTime) {
        this.factory = factory;
        this.codec = RecordCodecBuilder.mapCodec(
                p_300831_ -> p_300831_.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(p_300832_ -> p_300832_.group),
                        CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(p_300828_ -> p_300828_.category),
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(p_300833_ -> p_300833_.ingredient),
                        BuiltInRegistries.ITEM.byNameCodec().fieldOf("result").forGetter(p_300827_ -> p_300827_.result.getItem()),
                        Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(p_300826_ -> p_300826_.experience),
                        Codec.INT.fieldOf("cookingtime").orElse(cookingTime).forGetter(p_300834_ -> p_300834_.cookingTime))
                        .apply(p_300831_, factory::create));
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }

    @Override
    public MapCodec<T> codec() {
        return this.codec;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return this.streamCodec;
    }

    private T fromNetwork(RegistryFriendlyByteBuf buffer) {
        String s = buffer.readUtf();
        CookingBookCategory cookingbookcategory = buffer.readEnum(CookingBookCategory.class);
        Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        Item result = ByteBufCodecs.registry(Registries.ITEM).decode(buffer);
        float f = buffer.readFloat();
        int i = buffer.readVarInt();
        return this.factory.create(s, cookingbookcategory, ingredient, result, f, i);
    }

    private void toNetwork(RegistryFriendlyByteBuf buffer, T recipe) {
        buffer.writeUtf(recipe.group);
        buffer.writeEnum(recipe.category);
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
        ByteBufCodecs.registry(Registries.ITEM).encode(buffer, recipe.result.getItem());
        buffer.writeFloat(recipe.experience);
        buffer.writeVarInt(recipe.cookingTime);
    }

    public AbstractCookingRecipe create(
            String group, CookingBookCategory category, Ingredient ingredient, ItemLike result, float experience, int cookingTime) {
        return this.factory.create(group, category, ingredient, result, experience, cookingTime);
    }

    public interface Factory<T extends AbstractCookingRecipe> {
        T create(String group, CookingBookCategory category, Ingredient ingredient, ItemLike result, float experience, int cookingTime);
    }
}
