package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionEntityData;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record ConvertAndTameAbility(EntityType<?> newType, Optional<EntityPredicate> validEntities, Optional<EntityPredicate> invalidEntities, Optional<Holder<SoundEvent>> convertSound) implements TemptingAbility {

    public static final MapCodec<ConvertAndTameAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("new_type").forGetter(ConvertAndTameAbility::newType),
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(ConvertAndTameAbility::validEntities),
            EntityPredicate.CODEC.optionalFieldOf("invalid_entities").forGetter(ConvertAndTameAbility::invalidEntities),
            SoundEvent.CODEC.optionalFieldOf("convert_sound").forGetter(ConvertAndTameAbility::convertSound)).apply(instance, ConvertAndTameAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
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
                    entityData.putConverted(performer.getUUID(), target.getUUID(), tag);
                    target.discard();
                    level.addFreshEntity(newEntity);
                    Ability.playSound(level, performer, convertSound);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidEntity(ServerLevel level, Vec3 pos, Entity entity) {
        return validEntities.map(predicate -> predicate.matches(level, pos, entity)).orElse(true) && invalidEntities.map(predicate -> !predicate.matches(level, pos, entity)).orElse(true);
    }

    @Override
    public void revert(AbilityData data, ServerLevel level, Entity performer) {
        AbilityReversionEntityData.get(level).revertConversions(performer.getUUID(), level);
    }

    @Override
    public boolean shouldTempt(ServerLevel level, Vec3 pos, Entity entity) {
        return isValidEntity(level, pos, entity);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.CONVERT_AND_TAME.get();
    }
}
