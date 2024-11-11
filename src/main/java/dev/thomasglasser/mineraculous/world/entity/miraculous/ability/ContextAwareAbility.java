package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record ContextAwareAbility(Optional<Ability> blockAbility, Optional<Ability> entityAbility, Optional<Ability> itemAbility, Optional<Ability> airAbility, List<Ability> passiveAbilities) implements Ability {

    public static final MapCodec<ContextAwareAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.DIRECT_CODEC.optionalFieldOf("block").forGetter(ContextAwareAbility::blockAbility),
            Ability.DIRECT_CODEC.optionalFieldOf("entity").forGetter(ContextAwareAbility::entityAbility),
            Ability.DIRECT_CODEC.optionalFieldOf("item").forGetter(ContextAwareAbility::itemAbility),
            Ability.DIRECT_CODEC.optionalFieldOf("air").forGetter(ContextAwareAbility::airAbility),
            Ability.DIRECT_CODEC.listOf().optionalFieldOf("passive", List.of()).forGetter(ContextAwareAbility::passiveAbilities)).apply(instance, ContextAwareAbility::new));
    @Override
    public boolean perform(ResourceKey<Miraculous> type, MiraculousData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        return switch (context) {
            case INTERACT_BLOCK -> blockAbility.map(ability -> ability.perform(type, data, level, pos, performer, context)).orElse(false);
            case INTERACT_ENTITY -> entityAbility.map(ability -> ability.perform(type, data, level, pos, performer, context)).orElse(false);
            case INTERACT_ITEM -> itemAbility.map(ability -> ability.perform(type, data, level, pos, performer, context)).orElse(false);
            case INTERACT_AIR -> airAbility.map(ability -> ability.perform(type, data, level, pos, performer, context)).orElse(false);
            case PASSIVE -> {
                passiveAbilities.forEach(ability -> ability.perform(type, data, level, pos, performer, context));
                yield false;
            }
        };
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.CONTEXT_AWARE.get();
    }
}
