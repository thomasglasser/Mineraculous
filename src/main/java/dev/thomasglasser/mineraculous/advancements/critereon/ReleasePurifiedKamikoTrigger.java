package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class ReleasePurifiedKamikoTrigger extends SimpleCriterionTrigger<ReleasePurifiedKamikoTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int count) {
        this.trigger(player, instance -> instance.matches(count));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Integer> count) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(TriggerInstance::count))
                .apply(instance, TriggerInstance::new));

        public static Criterion<TriggerInstance> releasedPurifiedKamiko() {
            return criterion(Optional.empty(), Optional.empty());
        }

        public static Criterion<TriggerInstance> releasedPurifiedKamiko(int count) {
            return criterion(Optional.empty(), Optional.of(count));
        }

        private static Criterion<TriggerInstance> criterion(Optional<ContextAwarePredicate> player, Optional<Integer> count) {
            return MineraculousCriteriaTriggers.RELEASED_PURIFIED_KAMIKO.get().createCriterion(new TriggerInstance(player, count));
        }

        public boolean matches(int count) {
            return this.count.map(value -> value == count).orElse(true);
        }
    }
}
