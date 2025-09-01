package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Converts the target to the provided {@link EntityType} if it matches the provided {@link EntityPredicate}(s).
 *
 * @param newType         The {@link EntityType} to convert the target to
 * @param validEntities   The {@link EntityPredicate} the target must match
 * @param invalidEntities The {@link EntityPredicate} the target must not match
 * @param convertSound    The sound to play on successful conversion
 */
public record ConvertAndTameAbility(EntityType<?> newType, Optional<EntityPredicate> validEntities, Optional<EntityPredicate> invalidEntities, Optional<Holder<SoundEvent>> convertSound) implements TemptingAbility {

    public static final MapCodec<ConvertAndTameAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("new_type").forGetter(ConvertAndTameAbility::newType),
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(ConvertAndTameAbility::validEntities),
            EntityPredicate.CODEC.optionalFieldOf("invalid_entities").forGetter(ConvertAndTameAbility::invalidEntities),
            SoundEvent.CODEC.optionalFieldOf("convert_sound").forGetter(ConvertAndTameAbility::convertSound)).apply(instance, ConvertAndTameAbility::new));
    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context instanceof EntityAbilityContext(Entity target)) {
            AbilityReversionEntityData entityData = AbilityReversionEntityData.get(level);
            if (isValidEntity(level, target.position(), target) && !entityData.isConverted(target.getUUID())) {
                Entity newEntity = newType.create(level);
                if (newEntity != null) {
                    if (newEntity instanceof TamableAnimal tamable) {
                        tamable.setOwnerUUID(performer.getUUID());
                    }
                    newEntity.setUUID(target.getUUID());
                    newEntity.setPos(target.position());
                    CompoundTag tag = new CompoundTag();
                    target.save(tag);
                    entityData.putConverted(performer.getUUID(), target);
                    target.discard();
                    level.addFreshEntity(newEntity);
                    Ability.playSound(level, performer, convertSound);
                    return State.SUCCESS;
                }
            }
        }
        return State.FAIL;
    }

    private boolean isValidEntity(ServerLevel level, Vec3 pos, Entity entity) {
        return validEntities.map(predicate -> predicate.matches(level, pos, entity)).orElse(true) && invalidEntities.map(predicate -> !predicate.matches(level, pos, entity)).orElse(true);
    }

    @Override
    public void revert(AbilityData data, ServerLevel level, LivingEntity performer) {
        AbilityReversionEntityData.get(level).revertConversions(performer.getUUID(), level);
    }

    @Override
    public boolean shouldTempt(ServerLevel level, LivingEntity performer, Entity entity) {
        return isValidEntity(level, performer.position(), entity);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.CONVERT_AND_TAME.get();
    }
}
