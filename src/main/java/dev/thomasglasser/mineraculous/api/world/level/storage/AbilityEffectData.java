package dev.thomasglasser.mineraculous.api.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.ability.ContinuousAbility;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetFaceMaskTexturePayload;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Holds information used by existing {@link Ability}s.
 *
 * @param playedContinuousAbilityStartSound Whether the start sound of the current {@link ContinuousAbility} has been played
 * @param continuousTicks                   The current tick count of the current {@link ContinuousAbility} if present
 * @param shader                            The shader to force on the entity if present
 * @param faceMaskTexture                   The face mask texture to force on the entity if present
 * @param spectatingId                      The spectated entity to force on the entity if present
 * @param spectationInterrupted             Whether spectation should be interrupted for the entity
 * @param privateChat                       The entity to have private chat with if present
 * @param allowRemoteDamage                 Whether remote damage should be allowed from the entity to the spectated entity
 * @param allowKamikotizationRevocation     Whether the entity should be allowed to revoke the kamikotization of the entity they're spectating
 * @param killCredit                        The overridden kill credit for the entity if present
 */
public record AbilityEffectData(boolean playedContinuousAbilityStartSound, Optional<Integer> continuousTicks, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, Optional<UUID> spectatingId, boolean spectationInterrupted, Optional<UUID> privateChat, boolean allowRemoteDamage, boolean allowKamikotizationRevocation, Optional<UUID> killCredit, boolean toggleNightVision) {

    public static final StreamCodec<ByteBuf, AbilityEffectData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.BOOL, AbilityEffectData::playedContinuousAbilityStartSound,
            ByteBufCodecs.optional(ByteBufCodecs.INT), AbilityEffectData::continuousTicks,
            TommyLibExtraStreamCodecs.OPTIONAL_RESOURCE_LOCATION, AbilityEffectData::shader,
            TommyLibExtraStreamCodecs.OPTIONAL_RESOURCE_LOCATION, AbilityEffectData::faceMaskTexture,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), AbilityEffectData::spectatingId,
            ByteBufCodecs.BOOL, AbilityEffectData::spectationInterrupted,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), AbilityEffectData::privateChat,
            ByteBufCodecs.BOOL, AbilityEffectData::allowRemoteDamage,
            ByteBufCodecs.BOOL, AbilityEffectData::allowKamikotizationRevocation,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), AbilityEffectData::killCredit,
            ByteBufCodecs.BOOL, AbilityEffectData::toggleNightVision,
            AbilityEffectData::new);
    public AbilityEffectData() {
        this(false, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, Optional.empty(), false, false, Optional.empty(), false);
    }

    public AbilityEffectData stopContinuousAbility() {
        return new AbilityEffectData(false, Optional.empty(), shader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withSpectationInterrupted() {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, true, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withPlayedContinuousAbilityStartSound(boolean playedContinuousAbilityStartSound) {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withContinuousTicks(Optional<Integer> continuousTicks) {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withShader(Optional<ResourceLocation> nightVisionShader) {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, nightVisionShader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withFaceMaskTexture(Optional<ResourceLocation> faceMaskTexture) {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withPrivateChat(Optional<UUID> privateChat, Optional<ResourceLocation> faceMaskTexture) {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withSpectation(Optional<UUID> spectatingId, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, Optional<UUID> privateChat, boolean allowRemoteDamage, boolean allowKamikotizationRevocation) {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, false, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withKillCredit(Optional<UUID> killCredit) {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, toggleNightVision);
    }

    public AbilityEffectData withToggleNightVision() {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, true);
    }

    public AbilityEffectData updateNightVision(Optional<ResourceLocation> shader) {
        return new AbilityEffectData(playedContinuousAbilityStartSound, continuousTicks, shader, faceMaskTexture, spectatingId, spectationInterrupted, privateChat, allowRemoteDamage, allowKamikotizationRevocation, killCredit, false);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.ABILITY_EFFECTS, this);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.ABILITY_EFFECTS, this), entity.getServer());
    }

    public static boolean isInPrivateChat(Entity receiver, UUID senderId) {
        if (receiver.getUUID().equals(senderId))
            return true;
        Player sender = receiver.level().getPlayerByUUID(senderId);
        return (receiver.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).privateChat().map(chatter -> chatter.equals(senderId)).orElse(false) &&
                (sender != null && sender.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).privateChat().map(chatter -> chatter.equals(receiver.getUUID())).orElse(false)));
    }

    public static void removeFaceMaskTexture(Entity entity, Optional<ResourceLocation> faceMaskTexture) {
        AbilityEffectData abilityEffectData = entity.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
        abilityEffectData.faceMaskTexture().ifPresent(texture -> {
            if (texture.equals(faceMaskTexture.orElse(null))) {
                if (entity.level().isClientSide) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetFaceMaskTexturePayload(entity.getId(), Optional.empty()));
                } else {
                    abilityEffectData.withFaceMaskTexture(Optional.empty()).save(entity, true);
                }
            }
        });
    }
}
