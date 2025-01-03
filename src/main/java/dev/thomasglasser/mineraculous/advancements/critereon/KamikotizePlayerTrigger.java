package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

public class KamikotizePlayerTrigger extends SimpleCriterionTrigger<KamikotizePlayerTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return KamikotizePlayerTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ResourceKey<Kamikotization> type) {
        this.trigger(player, (instance) -> instance.matches(type));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ResourceKey<Kamikotization>> type)
            implements SimpleInstance {
        public static final Codec<KamikotizePlayerTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                p_337367_ -> p_337367_.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KamikotizePlayerTrigger.TriggerInstance::player),
                        ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("type").forGetter(KamikotizePlayerTrigger.TriggerInstance::type))
                        .apply(p_337367_, KamikotizePlayerTrigger.TriggerInstance::new));

        public static Criterion<KamikotizePlayerTrigger.TriggerInstance> kamikotizedPlayer() {
            return MineraculousCriteriaTriggers.KAMIKOTIZED_PLAYER.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public static Criterion<KamikotizePlayerTrigger.TriggerInstance> kamikotizedPlayer(ResourceKey<Kamikotization> type) {
            return MineraculousCriteriaTriggers.KAMIKOTIZED_PLAYER.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.of(type)));
        }

        public boolean matches(ResourceKey<Kamikotization> type) {
            return this.type.isEmpty() || this.type.get() == type;
        }
    }
}
