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
    public static void beginSpectation(LivingEntity entity, Optional<UUID> spectatingId, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, Optional<UUID> privateChat, boolean allowRemoteDamage, boolean allowKamikotizationRevocation) {
        entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).withSpectation(spectatingId, shader, faceMaskTexture, privateChat, allowRemoteDamage, allowKamikotizationRevocation).save(entity);
        entity.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withSpectationInterrupted(false).save(entity);
    }

    public static void endSpectation(LivingEntity entity) {
        entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).withSpectation(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, false).save(entity);
        entity.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withSpectationInterrupted(false).save(entity);
    }

    public static void updateNightVision(LivingEntity entity, Optional<ResourceLocation> shader) {
        entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).withShader(shader).save(entity);
        entity.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).withShouldToggleNightVision(false).save(entity);
    }

    public static boolean isMessageAllowed(Entity receiver, UUID senderId) {
        return receiver.getUUID().equals(senderId) || receiver.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).privateChat().map(id -> id.equals(senderId)).orElse(true);
    }

    public static void removeFaceMaskTexture(Entity entity, Optional<ResourceLocation> faceMaskTexture) {
        SyncedTransientAbilityEffectData syncedTransientAbilityEffectData = entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS);
        syncedTransientAbilityEffectData.faceMaskTexture().ifPresent(texture -> {
            if (texture.equals(faceMaskTexture.orElse(null))) {
                if (entity.level().isClientSide) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetFaceMaskTexturePayload(entity.getId(), Optional.empty()));
                } else {
                    syncedTransientAbilityEffectData.withFaceMaskTexture(Optional.empty()).save(entity);
                }
            }
        });
    }
}
