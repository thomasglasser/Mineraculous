package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundSetCameraEntityPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.Nullable;

/**
 * Toggles spectation of a valid target.
 *
 * @param validEntities The {@link EntityPredicate} the entity must match
 * @param invalidEntities The {@link EntityPredicate} the entity must not match
 * @param privateChat Whether spectation should disallow messages not from the performer or target
 * @param allowRemoteDamage Whether spectation should allow remote damage from performer to target on performer swing
 * @param shader The shader to apply to the performer on spectation
 * @param faceMaskTexture The face mask texture to apply to the performer on spectation
 * @param startSound The sound to play when spectation begins
 * @param stopSound The sound to play when spectation ends
 */
public record SpectateEntityAbility(Optional<EntityPredicate> validEntities, Optional<EntityPredicate> invalidEntities, boolean privateChat, boolean allowRemoteDamage, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, Optional<Holder<SoundEvent>> startSound, Optional<Holder<SoundEvent>> stopSound) implements Ability {

    public static final MapCodec<SpectateEntityAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(SpectateEntityAbility::validEntities),
            EntityPredicate.CODEC.optionalFieldOf("invalid_entities").forGetter(SpectateEntityAbility::invalidEntities),
            Codec.BOOL.optionalFieldOf("private_chat", false).forGetter(SpectateEntityAbility::privateChat),
            Codec.BOOL.optionalFieldOf("allow_remote_damage", false).forGetter(SpectateEntityAbility::allowRemoteDamage),
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(SpectateEntityAbility::shader),
            ResourceLocation.CODEC.optionalFieldOf("face_mask_texture").forGetter(SpectateEntityAbility::faceMaskTexture),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(SpectateEntityAbility::startSound),
            SoundEvent.CODEC.optionalFieldOf("stop_sound").forGetter(SpectateEntityAbility::stopSound)).apply(instance, SpectateEntityAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context == null) {
            AbilityEffectData abilityEffectData = performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
            if (abilityEffectData.spectationInterrupted()) {
                stopSpectation(level, performer);
                return true;
            } else if (data.powerActive()) {
                if (abilityEffectData.spectatingId().isPresent()) {
                    stopSpectation(level, performer);
                    return true;
                } else {
                    List<? extends Entity> entities = level.getEntities(EntityTypeTest.forClass(Entity.class), entity -> isValidEntity(level, performer, entity));
                    if (!entities.isEmpty()) {
                        Entity target = entities.getFirst();
                        abilityEffectData.withSpectation(Optional.of(target.getUUID()), shader, faceMaskTexture, privateChat ? Optional.of(target.getUUID()) : Optional.empty(), allowRemoteDamage).save(performer, true);
                        if (privateChat) {
                            abilityEffectData.withPrivateChat(Optional.of(performer.getUUID()), faceMaskTexture).save(target, true);
                        }
                        if (performer instanceof ServerPlayer player) {
                            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(Optional.of(target.getId())), player);
                        }
                        Ability.playSound(level, performer, startSound);
                        return true;
                    }
                }
            } else {
                return abilityEffectData.spectatingId().isPresent();
            }
        }
        return false;
    }

    private boolean isValidEntity(ServerLevel level, Entity performer, Entity target) {
        return performer != target && validEntities.map(predicate -> predicate.matches(level, performer.position(), target)).orElse(true) && invalidEntities.map(predicate -> !predicate.matches(level, performer.position(), target)).orElse(false);
    }

    private void stopSpectation(ServerLevel level, Entity performer) {
        performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withSpectation(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false).save(performer, true);
        if (performer instanceof ServerPlayer player) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(Optional.empty()), player);
        }
        Ability.playSound(level, performer, stopSound);
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, Entity performer) {
        stopSpectation(level, performer);
    }

    @Override
    public void leaveLevel(AbilityData data, ServerLevel level, Entity performer) {
        stopSpectation(level, performer);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.SPECTATE_ENTITY.get();
    }
}
