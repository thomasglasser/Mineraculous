package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record SetOwnerAbility(Optional<Integer> maxOfTypes, Optional<EntityPredicate> validEntities, Optional<EntityPredicate> invalidEntities) implements Ability {

    public static final MapCodec<SetOwnerAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("max_of_types").forGetter(SetOwnerAbility::maxOfTypes),
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(SetOwnerAbility::validEntities),
            EntityPredicate.CODEC.optionalFieldOf("invalid_entities").forGetter(SetOwnerAbility::invalidEntities)).apply(instance, SetOwnerAbility::new));
    @Override
    public boolean perform(ResourceKey<Miraculous> type, MiraculousData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.INTERACT_ENTITY && context.entity() instanceof TamableAnimal animal && animal.level() instanceof ServerLevel serverLevel) {
            if (validEntities.isPresent() && !validEntities.get().matches(serverLevel, animal.position(), animal))
                return false;
            if (invalidEntities.isPresent() && invalidEntities.get().matches(serverLevel, animal.position(), animal))
                return false;
            if (maxOfTypes.isPresent() && level.getEntitiesOfClass(animal.getClass(), AABB.INFINITE, entity -> {
                if (validEntities.isPresent() && !validEntities.get().matches(serverLevel, entity.position(), entity))
                    return false;
                if (invalidEntities.isPresent() && invalidEntities.get().matches(serverLevel, entity.position(), entity))
                    return false;
                return entity.getType() == animal.getType() && entity.isOwnedBy(performer);
            }).size() >= maxOfTypes.get())
                return false;
            animal.setOwnerUUID(performer.getUUID());
            return true;
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.SET_OWNER.get();
    }
}
