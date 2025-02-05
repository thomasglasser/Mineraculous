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
    public Codec<ReleasePurifiedKamikoTrigger.TriggerInstance> codec() {
        return ReleasePurifiedKamikoTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int count) {
        this.trigger(player, (instance) -> instance.matches(count));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Integer> count)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ReleasePurifiedKamikoTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                p_337367_ -> p_337367_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ReleasePurifiedKamikoTrigger.TriggerInstance::player),
                        ExtraCodecs.POSITIVE_INT.optionalFieldOf("count").forGetter(ReleasePurifiedKamikoTrigger.TriggerInstance::count))
                        .apply(p_337367_, ReleasePurifiedKamikoTrigger.TriggerInstance::new));

        public static Criterion<ReleasePurifiedKamikoTrigger.TriggerInstance> releasedPurifiedKamiko() {
            return MineraculousCriteriaTriggers.RELEASED_PURIFIED_KAMIKO.get().createCriterion(new ReleasePurifiedKamikoTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public static Criterion<ReleasePurifiedKamikoTrigger.TriggerInstance> releasedPurifiedKamiko(int count) {
            return MineraculousCriteriaTriggers.RELEASED_PURIFIED_KAMIKO.get().createCriterion(new ReleasePurifiedKamikoTrigger.TriggerInstance(Optional.empty(), Optional.of(count)));
        }

        public boolean matches(int count) {
            return this.count.isEmpty() || this.count.get() == count;
        }
    }
}
