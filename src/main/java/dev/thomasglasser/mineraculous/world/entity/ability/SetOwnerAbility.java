package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

public record SetOwnerAbility(Optional<Integer> maxOfTypes, Optional<EntityPredicate> validEntities, Optional<EntityPredicate> invalidEntities, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final MapCodec<SetOwnerAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_of_types").forGetter(SetOwnerAbility::maxOfTypes),
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(SetOwnerAbility::validEntities),
            EntityPredicate.CODEC.optionalFieldOf("invalid_entities").forGetter(SetOwnerAbility::invalidEntities),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(SetOwnerAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(SetOwnerAbility::overrideActive)).apply(instance, SetOwnerAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.INTERACT_ENTITY && context.entity() instanceof TamableAnimal animal) {
            if (validEntities.isPresent() && !validEntities.get().matches(level, animal.position(), animal))
                return false;
            if (invalidEntities.isPresent() && invalidEntities.get().matches(level, animal.position(), animal))
                return false;
            animal.setOwnerUUID(entity.getUUID());
            playStartSound(level, pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        List<Entity> like = new ArrayList<>();
        for (Entity e : level.getEntities().getAll()) {
            if (isValid(e) && (entity instanceof TamableAnimal ta && ta.isOwnedBy(entity)))
                like.add(e);
        }
        return maxOfTypes.isEmpty() || like.size() < maxOfTypes.get();
    }

    public boolean isValid(Entity entity) {
        ServerLevel level = (ServerLevel) entity.level();
        return (validEntities.isEmpty() || validEntities.get().matches(level, entity.position(), entity))
                && (invalidEntities.isEmpty() || !invalidEntities.get().matches(level, entity.position(), entity));
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.SET_OWNER.get();
    }
}
