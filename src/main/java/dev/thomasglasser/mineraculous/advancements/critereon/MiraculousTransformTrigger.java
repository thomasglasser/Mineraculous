package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

public class MiraculousTransformTrigger extends SimpleCriterionTrigger<MiraculousTransformTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return MiraculousTransformTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MiraculousType type) {
        this.trigger(player, (instance) -> instance.matches(type));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MiraculousType type)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<MiraculousTransformTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                p_337367_ -> p_337367_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MiraculousTransformTrigger.TriggerInstance::player),
                        StringRepresentable.fromEnum(MiraculousType::values).fieldOf("type").forGetter(MiraculousTransformTrigger.TriggerInstance::type))
                        .apply(p_337367_, MiraculousTransformTrigger.TriggerInstance::new));

        public static Criterion<MiraculousTransformTrigger.TriggerInstance> transformed(MiraculousType type) {
            return MineraculousCriteriaTriggers.TRANSFORMED_MIRACULOUS.get().createCriterion(new TriggerInstance(Optional.empty(), type));
        }

        public boolean matches(MiraculousType type) {
            return this.type == type;
        }
    }
}
