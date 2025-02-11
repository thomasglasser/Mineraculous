package dev.thomasglasser.mineraculous.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
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
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof Player player)
            return player.getInventory().hasAnyMatching(invert ? predicate.negate() : predicate);
        else if (entity instanceof InventoryCarrier carrier)
            return carrier.getInventory().hasAnyMatching(invert ? predicate.negate() : predicate);
        else if (entity instanceof LivingEntity livingEntity) {
            Predicate<ItemStack> p = invert ? predicate.negate() : predicate;
            boolean has = false;
            for (ItemStack stack : livingEntity.getAllSlots()) {
                if (p.test(stack)) {
                    has = true;
                    break;
                }
            }
            return has;
        }
        return false;
    }

    @Override
    public LootItemConditionType getType() {
        return MineraculousLootItemConditions.HAS_ITEM.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
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
