package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundSetCameraEntityPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.Nullable;

public record SpectateEntityAbility(EntityPredicate validEntities, boolean privateChat, boolean allowRemoteDamage, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture) implements Ability {

    public static final MapCodec<SpectateEntityAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityPredicate.CODEC.fieldOf("valid_entities").forGetter(SpectateEntityAbility::validEntities),
            Codec.BOOL.optionalFieldOf("private_chat", false).forGetter(SpectateEntityAbility::privateChat),
            Codec.BOOL.optionalFieldOf("allow_remote_damage", false).forGetter(SpectateEntityAbility::allowRemoteDamage),
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(SpectateEntityAbility::shader),
            ResourceLocation.CODEC.optionalFieldOf("face_mask_texture").forGetter(SpectateEntityAbility::faceMaskTexture)).apply(instance, SpectateEntityAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context == null) {
            AbilityEffectData abilityEffectData = performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
            if (abilityEffectData.spectationInterrupted()) {
                stopSpectation(performer);
                return true;
            } else if (data.powerActive()) {
                if (abilityEffectData.spectatingId().isPresent()) {
                    stopSpectation(performer);
                    return true;
                } else {
                    List<? extends Entity> entities = level.getEntities(EntityTypeTest.forClass(Entity.class), entity -> validEntities.matches(level, performer.position(), entity));
                    if (!entities.isEmpty()) {
                        Entity target = entities.getFirst();
                        abilityEffectData.withSpectation(Optional.of(target.getUUID()), shader, faceMaskTexture, privateChat ? Optional.of(target.getUUID()) : Optional.empty(), allowRemoteDamage).save(performer, true);
                        abilityEffectData.withPrivateChat(Optional.of(performer.getUUID()), faceMaskTexture).save(target, true);
                        if (performer instanceof ServerPlayer player) {
                            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(Optional.of(target.getId())), player);
                        }
                        return true;
                    }
                }
            } else {
                return abilityEffectData.spectatingId().isPresent();
            }
        }
        return false;
    }

    private void stopSpectation(Entity performer) {
        performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withSpectation(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false).save(performer, true);
        if (performer instanceof ServerPlayer player) {
            TommyLibServices.NETWORK.sendToClient(new ClientboundSetCameraEntityPayload(Optional.empty()), player);
        }
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, Entity performer) {
        stopSpectation(performer);
    }

    @Override
    public void leaveLevel(AbilityData data, ServerLevel level, Entity performer) {
        stopSpectation(performer);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.SPECTATE_ENTITY.get();
    }
}
