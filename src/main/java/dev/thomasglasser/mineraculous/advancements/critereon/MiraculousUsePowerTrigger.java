package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

public class MiraculousUsePowerTrigger extends SimpleCriterionTrigger<MiraculousUsePowerTrigger.TriggerInstance> {
    @Override
    public Codec<MiraculousUsePowerTrigger.TriggerInstance> codec() {
        return MiraculousUsePowerTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MiraculousType type, Context context) {
        this.trigger(player, (instance) -> instance.matches(type, context));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MiraculousType type, List<Context> contexts)
            implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<MiraculousUsePowerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                p_337367_ -> p_337367_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MiraculousUsePowerTrigger.TriggerInstance::player),
                        StringRepresentable.fromEnum(MiraculousType::values).fieldOf("type").forGetter(MiraculousUsePowerTrigger.TriggerInstance::type),
                        StringRepresentable.fromEnum(Context::values).listOf().fieldOf("contexts").forGetter(MiraculousUsePowerTrigger.TriggerInstance::contexts))
                        .apply(p_337367_, MiraculousUsePowerTrigger.TriggerInstance::new));
        public static Criterion<MiraculousUsePowerTrigger.TriggerInstance> usedPower(MiraculousType type) {
            return usedPower(type, Context.any());
        }

        public static Criterion<MiraculousUsePowerTrigger.TriggerInstance> usedPower(MiraculousType type, Context... contexts) {
            return usedPower(type, List.of(contexts));
        }

        public static Criterion<MiraculousUsePowerTrigger.TriggerInstance> usedPower(MiraculousType type, List<Context> contexts) {
            return MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().createCriterion(new TriggerInstance(Optional.empty(), type, contexts));
        }

        public boolean matches(MiraculousType type, Context context) {
            return this.type == type && this.contexts.contains(context);
        }
    }

    public enum Context implements StringRepresentable {
        BLOCK,
        ENTITY,
        LIVING_ENTITY;

        public static List<Context> any() {
            return List.of(values());
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }
}
