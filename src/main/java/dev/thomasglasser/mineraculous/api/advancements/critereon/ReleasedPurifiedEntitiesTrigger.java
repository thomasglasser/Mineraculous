package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Advancement trigger for when purified entities are released (from the {@link LadybugYoyoItem} by default),
 * with optional fields for entity predicates and count.
 */
public class ReleasedPurifiedEntitiesTrigger extends SimpleCriterionTrigger<ReleasedPurifiedEntitiesTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Collection<? extends Entity> released) {
        List<LootContext> releaseContexts = new ReferenceArrayList<>();
        for (Entity entity : released) {
            releaseContexts.add(EntityPredicate.createContext(player, entity));
        }
        this.trigger(player, instance -> instance.matches(releaseContexts));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> released, Optional<Integer> count) implements SimpleInstance {

        static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("released", ImmutableList.of()).forGetter(TriggerInstance::released),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(TriggerInstance::count))
                .apply(instance, TriggerInstance::new));
        public static Criterion<TriggerInstance> releasedPurifiedEntities() {
            return releasedPurifiedEntities(Optional.empty(), ImmutableList.of(), Optional.empty());
        }

        public static Criterion<TriggerInstance> releasedPurifiedEntities(int count) {
            return releasedPurifiedEntities(Optional.empty(), ImmutableList.of(), Optional.of(count));
        }

        public static Criterion<TriggerInstance> releasedPurifiedEntities(EntityPredicate.Builder... released) {
            return releasedPurifiedEntities(Optional.empty(), EntityPredicate.wrap(released), Optional.empty());
        }

        public static Criterion<TriggerInstance> releasedPurifiedEntities(int count, EntityPredicate.Builder... released) {
            return releasedPurifiedEntities(Optional.empty(), EntityPredicate.wrap(released), Optional.of(count));
        }

        private static Criterion<TriggerInstance> releasedPurifiedEntities(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> released, Optional<Integer> count) {
            return MineraculousCriteriaTriggers.RELEASED_PURIFIED_ENTITIES.get().createCriterion(new TriggerInstance(player, released, count));
        }

        public boolean matches(List<LootContext> released) {
            for (ContextAwarePredicate predicate : this.released) {
                boolean flag = false;

                for (LootContext context : released) {
                    if (predicate.matches(context)) {
                        flag = true;
                        break;
                    }
                }

                if (!flag) {
                    return false;
                }
            }
            return this.count.map(value -> value == released.size()).orElse(true);
        }
    }
}
