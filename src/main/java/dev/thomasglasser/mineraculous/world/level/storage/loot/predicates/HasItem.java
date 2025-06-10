package dev.thomasglasser.mineraculous.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record HasItem(ItemPredicate predicate, boolean invert) implements LootItemCondition {
    public static final MapCodec<HasItem> CODEC = RecordCodecBuilder.mapCodec(p_345271_ -> p_345271_.group(
            ItemPredicate.CODEC.fieldOf("predicate").forGetter(HasItem::predicate),
            Codec.BOOL.optionalFieldOf("invert", false).forGetter(HasItem::invert)).apply(p_345271_, HasItem::new));

    public boolean test(LootContext context) {
        Predicate<ItemStack> predicate = invert ? this.predicate.negate() : this.predicate;
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        for (ItemStack stack : EntityUtils.getInventory(entity)) {
            if (predicate.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LootItemConditionType getType() {
        return MineraculousLootItemConditions.HAS_ITEM.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ReferenceOpenHashSet.of(LootContextParams.THIS_ENTITY);
    }

    public static LootItemCondition.Builder hasItemsMatching(ItemPredicate predicate) {
        return () -> new HasItem(predicate, false);
    }

    public static LootItemCondition.Builder hasItemsMatching(ItemPredicate.Builder predicateBuilder) {
        return () -> new HasItem(predicateBuilder.build(), false);
    }

    public static LootItemCondition.Builder hasNoItemsMatching(ItemPredicate predicate) {
        return () -> new HasItem(predicate, true);
    }

    public static LootItemCondition.Builder hasNoItemsMatching(ItemPredicate.Builder predicateBuilder) {
        return () -> new HasItem(predicateBuilder.build(), true);
    }
}
