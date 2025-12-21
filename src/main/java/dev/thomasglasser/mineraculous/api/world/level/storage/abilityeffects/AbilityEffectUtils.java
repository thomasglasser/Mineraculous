package dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetFaceMaskTexturePayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AbilityEffectUtils {
    /**
     * Begin spectating for the given entity.
     * 
     * @param entity                        The spectating entity
     * @param spectatingId                  The entity to spectate
     * @param shader                        The shader to use
     * @param faceMaskTexture               The face mask texture to use
     * @param privateChat                   Whether the spectator should have private chat with the spectated entity
     * @param allowRemoteDamage             Whether remote damage should be allowed from the spectator to the spectated entity
     * @param allowKamikotizationRevocation Whether the spectator should be allowed to revoke the kamikotization of the spectated entity
     */
    public static void beginSpectation(LivingEntity entity, Optional<UUID> spectatingId, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, Optional<UUID> privateChat, boolean allowRemoteDamage, boolean allowKamikotizationRevocation) {
        entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).withSpectation(spectatingId, shader, faceMaskTexture, privateChat, allowRemoteDamage, allowKamikotizationRevocation).save(entity);
        entity.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withSpectationInterrupted(false).save(entity);
    }

    /**
     * End spectating for the given entity.
     *
     * @param entity The entity to end spectating for
     */
    public static void endSpectation(LivingEntity entity) {
        entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).withSpectation(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, false).save(entity);
        entity.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withSpectationInterrupted(false).save(entity);
    }

    /**
     * Update the shader for the given entity and mark night vision toggled.
     *
     * @param entity The entity to update the shader for
     * @param shader The shader to use
     */
    public static void updateNightVision(LivingEntity entity, Optional<ResourceLocation> shader) {
        entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).withShader(shader).save(entity);
        entity.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withShouldToggleNightVision(false).save(entity);
    }

    /**
     * Determines whether the given receiver is allowed to receive a message from the given sender.
     *
     * @param receiver The receiver of the message
     * @param senderId The sender of the message
     * @return Whether the receiver is allowed to receive the message
     */
    public static boolean isMessageAllowed(Entity receiver, UUID senderId) {
        return receiver.getUUID().equals(senderId) || receiver.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).privateChat().map(id -> id.equals(senderId)).orElse(true);
    }

    /**
     * Removes the face mask texture from the given entity.
     *
     * @param entity          The entity to remove the face mask texture from
     * @param faceMaskTexture The face mask texture to remove
     */
    public static void removeFaceMaskTexture(Entity entity, Optional<ResourceLocation> faceMaskTexture) {
        SyncedTransientAbilityEffectData syncedTransientAbilityEffectData = entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS);
        syncedTransientAbilityEffectData.faceMaskTexture().ifPresent(texture -> {
            if (faceMaskTexture.map(texture::equals).orElse(false)) {
                if (entity.level().isClientSide) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetFaceMaskTexturePayload(entity.getId(), Optional.empty()));
                } else {
                    syncedTransientAbilityEffectData.withFaceMaskTexture(Optional.empty()).save(entity);
                }
            }
        });
    }
}
