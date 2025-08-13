package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.level.storage.loot.entries;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TagEntry.class)
public abstract class TagEntryMixin extends LootPoolSingletonContainer {
    private TagEntryMixin(int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
    }

    @Redirect(method = "expandTag(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)Z", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private <T> void fixTagEnchantments(Consumer<T> instance, T t, @Local(argsOnly = true) LootContext context, @Local Holder<Item> holder) {
        instance.accept((T) new EntryBase() {
            @Override
            public void createItemStack(Consumer<ItemStack> stackConsumer, LootContext lootContext) {
                LootItemFunction.decorate(LootItemFunctions.compose(functions), stackConsumer, lootContext).accept(new ItemStack(holder));
            }
        });
    }
}
