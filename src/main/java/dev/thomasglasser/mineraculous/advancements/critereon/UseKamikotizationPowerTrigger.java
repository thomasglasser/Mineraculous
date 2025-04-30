package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

public class UseKamikotizationPowerTrigger extends SimpleCriterionTrigger<UseKamikotizationPowerTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Kamikotization> type, Context context) {
        this.trigger(player, instance -> instance.matches(type, context));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, ResourceKey<Kamikotization> type, List<Context> contexts) implements SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).fieldOf("type").forGetter(TriggerInstance::type),
                Context.CODEC.listOf().optionalFieldOf("contexts", Context.any()).forGetter(TriggerInstance::contexts))
                .apply(instance, TriggerInstance::new));
        public static Criterion<TriggerInstance> usedPower(ResourceKey<Kamikotization> type) {
            return usedPower(type, Context.any());
        }

        public static Criterion<TriggerInstance> usedPower(ResourceKey<Kamikotization> type, Context... contexts) {
            return usedPower(type, List.of(contexts));
        }

        public static Criterion<TriggerInstance> usedPower(ResourceKey<Kamikotization> type, List<Context> contexts) {
            return criterion(Optional.empty(), type, contexts);
        }

        public static Criterion<TriggerInstance> criterion(Optional<ContextAwarePredicate> player, ResourceKey<Kamikotization> type, List<Context> contexts) {
            return MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().createCriterion(new TriggerInstance(player, type, contexts));
        }

        public boolean matches(ResourceKey<Kamikotization> type, Context context) {
            return this.type == type && this.contexts.contains(context);
        }
    }

    public enum Context implements StringRepresentable {
        EMPTY,
        BLOCK,
        ENTITY,
        LIVING_ENTITY,
        ITEM;

        private static final Codec<Context> CODEC = StringRepresentable.fromEnum(Context::values);

        public static List<Context> any() {
            return List.of(values());
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }
}
