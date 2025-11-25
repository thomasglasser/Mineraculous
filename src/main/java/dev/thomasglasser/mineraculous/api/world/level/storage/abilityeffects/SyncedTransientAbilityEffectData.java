package dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects;

import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Holds information used by existing {@link Ability}s that are synced to the client.
 *
 * @param shader                        The shader to force on the entity if present
 * @param faceMaskTexture               The face mask texture to force on the entity if present
 * @param spectatingId                  The spectated entity to force on the entity if present
 * @param privateChat                   The entity to have the private chat with if present
 * @param allowRemoteDamage             Whether remote damage should be allowed from the entity to the spectated entity
 * @param allowKamikotizationRevocation Whether the entity should be allowed to revoke the kamikotization of the entity they're spectating
 */
public record SyncedTransientAbilityEffectData(Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, Optional<UUID> spectatingId, Optional<UUID> privateChat, boolean allowRemoteDamage, boolean allowKamikotizationRevocation) {

    public static final StreamCodec<ByteBuf, SyncedTransientAbilityEffectData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), SyncedTransientAbilityEffectData::shader,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), SyncedTransientAbilityEffectData::faceMaskTexture,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), SyncedTransientAbilityEffectData::spectatingId,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), SyncedTransientAbilityEffectData::privateChat,
            ByteBufCodecs.BOOL, SyncedTransientAbilityEffectData::allowRemoteDamage,
            ByteBufCodecs.BOOL, SyncedTransientAbilityEffectData::allowKamikotizationRevocation,
            SyncedTransientAbilityEffectData::new);
    public SyncedTransientAbilityEffectData() {
        this(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), false, false);
    }

    public SyncedTransientAbilityEffectData withShader(Optional<ResourceLocation> shader) {
        return new SyncedTransientAbilityEffectData(shader, faceMaskTexture, spectatingId, privateChat, allowRemoteDamage, allowKamikotizationRevocation);
    }

    public SyncedTransientAbilityEffectData withFaceMaskTexture(Optional<ResourceLocation> faceMaskTexture) {
        return new SyncedTransientAbilityEffectData(shader, faceMaskTexture, spectatingId, privateChat, allowRemoteDamage, allowKamikotizationRevocation);
    }

    public SyncedTransientAbilityEffectData withPrivateChat(Optional<UUID> privateChat, Optional<ResourceLocation> faceMaskTexture) {
        return new SyncedTransientAbilityEffectData(shader, faceMaskTexture, spectatingId, privateChat, allowRemoteDamage, allowKamikotizationRevocation);
    }

    public SyncedTransientAbilityEffectData withSpectation(Optional<UUID> spectatingId, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, Optional<UUID> privateChat, boolean allowRemoteDamage, boolean allowKamikotizationRevocation) {
        return new SyncedTransientAbilityEffectData(shader, faceMaskTexture, spectatingId, privateChat, allowRemoteDamage, allowKamikotizationRevocation);
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS, this);
    }
}
