package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import org.jetbrains.annotations.Nullable;

/**
 * Converts the target to the provided {@link EntityType} if it matches the provided {@link EntityPredicate}(s).
 *
 * @param newType         The {@link EntityType} to convert the target to
 * @param validEntities   The {@link EntityPredicate} the target must match
 * @param invalidEntities The {@link EntityPredicate} the target must not match
 * @param convertSound    The sound to play on successful conversion
 */
public record ConvertAndTameAbility(EntityType<?> newType, boolean requireNoneStored, Optional<EntityPredicate> validEntities, Optional<EntityPredicate> invalidEntities, Optional<Holder<SoundEvent>> convertSound) implements Ability {

    public static final MapCodec<ConvertAndTameAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("new_type").forGetter(ConvertAndTameAbility::newType),
            Codec.BOOL.optionalFieldOf("require_none_stored", false).forGetter(ConvertAndTameAbility::requireNoneStored),
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(ConvertAndTameAbility::validEntities),
            EntityPredicate.CODEC.optionalFieldOf("invalid_entities").forGetter(ConvertAndTameAbility::invalidEntities),
            SoundEvent.CODEC.optionalFieldOf("convert_sound").forGetter(ConvertAndTameAbility::convertSound)).apply(instance, ConvertAndTameAbility::new));
    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context instanceof EntityAbilityContext(Entity target)) {
            if (!isValidEntity(level, performer, target))
                return State.CANCEL;
            EntityReversionData entityData = EntityReversionData.get(level);
            boolean hasStored = false;
            if (requireNoneStored) {
                List<CompoundTag> stored = data.storedEntities();
                for (CompoundTag tag : stored) {
                    if (tag.contains("UUID")) {
                        UUID uuid = tag.getUUID("UUID");
                        if (entityData.isConvertedOrCopied(uuid)) {
                            hasStored = true;
                            break;
                        }
                    }
                }
            }
            if (!(requireNoneStored && hasStored) && !entityData.isConvertedOrCopied(target.getUUID())) {
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
                    return State.CONSUME;
                }
            }
            return State.CANCEL;
        }
        return State.PASS;
    }

    /**
     * Determines if the provided target is valid for the ability.
     *
     * @param level     The level the target is in
     * @param performer The performer of the ability
     * @param target    The target to check validity
     * @return Whether the target is valid for the ability
     */
    public boolean isValidEntity(ServerLevel level, LivingEntity performer, Entity target) {
        return performer != target && validEntities.map(predicate -> predicate.matches(level, performer.position(), target)).orElse(true) && invalidEntities.map(predicate -> !predicate.matches(level, performer.position(), target)).orElse(true);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.CONVERT_AND_TAME.get();
    }
}
