package dev.thomasglasser.mineraculous.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParams;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record MatchShooter(Optional<ItemPredicate> predicate) implements LootItemCondition {
    public static final MapCodec<MatchShooter> CODEC = RecordCodecBuilder.mapCodec(
            p_338172_ -> p_338172_.group(ItemPredicate.CODEC.optionalFieldOf("predicate").forGetter(MatchShooter::predicate)).apply(p_338172_, MatchShooter::new));

    @Override
    public LootItemConditionType getType() {
        return MineraculousLootItemConditions.SHOOTER_MATCHES.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(MineraculousLootContextParams.SHOOTER);
    }

    public boolean test(LootContext context) {
        ItemStack itemstack = context.getParamOrNull(MineraculousLootContextParams.SHOOTER);
        return itemstack != null && (this.predicate.isEmpty() || this.predicate.get().test(itemstack));
    }

    public static LootItemCondition.Builder shooterMatches(ItemPredicate.Builder toolPredicateBuilder) {
        return () -> new MatchShooter(Optional.of(toolPredicateBuilder.build()));
    }
}
