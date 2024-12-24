package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

public record SetOwnerAbility(Optional<Integer> maxOfTypes, Optional<EntityPredicate> validEntities, Optional<EntityPredicate> invalidEntities, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final MapCodec<SetOwnerAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("max_of_types").forGetter(SetOwnerAbility::maxOfTypes),
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(SetOwnerAbility::validEntities),
            EntityPredicate.CODEC.optionalFieldOf("invalid_entities").forGetter(SetOwnerAbility::invalidEntities),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(SetOwnerAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(SetOwnerAbility::overrideActive)).apply(instance, SetOwnerAbility::new));
    @Override
    public boolean perform(AbilityData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.INTERACT_ENTITY && context.entity() instanceof TamableAnimal animal && animal.level() instanceof ServerLevel serverLevel) {
            if (validEntities.isPresent() && !validEntities.get().matches(serverLevel, animal.position(), animal))
                return false;
            if (invalidEntities.isPresent() && invalidEntities.get().matches(serverLevel, animal.position(), animal))
                return false;
            List<Entity> like = new ArrayList<>();
            for (Entity e : serverLevel.getEntities().getAll()) {
                if ((validEntities.isEmpty() || validEntities.get().matches(serverLevel, e.position(), e))
                        && (invalidEntities.isEmpty() || !invalidEntities.get().matches(serverLevel, e.position(), e))
                        && (e instanceof TamableAnimal ta && ta.isOwnedBy(performer)))
                    like.add(e);
            }
            if (maxOfTypes.isPresent() && like.size() >= maxOfTypes.get())
                return false;
            animal.setOwnerUUID(performer.getUUID());
            playStartSound(level, pos);
            return true;
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.SET_OWNER.get();
    }
}
