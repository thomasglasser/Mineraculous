package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.network.ServerboundSetFaceMaskTexturePayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
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

public record AbilityEffectData(Optional<Integer> dragTicks, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, boolean isSpectating, boolean spectationInterrupted, Optional<UUID> privateChat, boolean allowRemoteDamage, Optional<UUID> killCredit) {

    public static final StreamCodec<ByteBuf, AbilityEffectData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT), AbilityEffectData::dragTicks,
            TommyLibExtraStreamCodecs.OPTIONAL_RESOURCE_LOCATION, AbilityEffectData::shader,
            TommyLibExtraStreamCodecs.OPTIONAL_RESOURCE_LOCATION, AbilityEffectData::faceMaskTexture,
            ByteBufCodecs.BOOL, AbilityEffectData::isSpectating,
            ByteBufCodecs.BOOL, AbilityEffectData::spectationInterrupted,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), AbilityEffectData::privateChat,
            ByteBufCodecs.BOOL, AbilityEffectData::allowRemoteDamage,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), AbilityEffectData::killCredit,
            AbilityEffectData::new);
    public AbilityEffectData() {
        this(Optional.empty(), Optional.empty(), Optional.empty(), false, false, Optional.empty(), false, Optional.empty());
    }

    public AbilityEffectData withDragTicks(Optional<Integer> dragTicks) {
        return new AbilityEffectData(dragTicks, shader, faceMaskTexture, isSpectating, spectationInterrupted, privateChat, allowRemoteDamage, killCredit);
    }

    public AbilityEffectData withShader(Optional<ResourceLocation> nightVisionShader) {
        return new AbilityEffectData(dragTicks, nightVisionShader, faceMaskTexture, isSpectating, spectationInterrupted, privateChat, allowRemoteDamage, killCredit);
    }

    public AbilityEffectData withFaceMaskTexture(Optional<ResourceLocation> faceMaskTexture) {
        return new AbilityEffectData(dragTicks, shader, faceMaskTexture, isSpectating, spectationInterrupted, privateChat, allowRemoteDamage, killCredit);
    }

    public AbilityEffectData withPrivateChat(Optional<UUID> privateChat, Optional<ResourceLocation> faceMaskTexture) {
        return new AbilityEffectData(dragTicks, shader, faceMaskTexture, isSpectating, spectationInterrupted, privateChat, allowRemoteDamage, killCredit);
    }

    public AbilityEffectData withKamikoControl(boolean isSpectating, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture) {
        return new AbilityEffectData(dragTicks, shader, faceMaskTexture, isSpectating, false, privateChat, allowRemoteDamage, killCredit);
    }

    public AbilityEffectData withSpectationInterrupted() {
        return new AbilityEffectData(dragTicks, shader, faceMaskTexture, isSpectating, true, privateChat, allowRemoteDamage, killCredit);
    }

    public AbilityEffectData withSpectation(boolean isSpectating, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, Optional<UUID> privateChat, boolean allowRemoteDamage) {
        return new AbilityEffectData(dragTicks, shader, faceMaskTexture, isSpectating, spectationInterrupted, privateChat, allowRemoteDamage, killCredit);
    }

    public AbilityEffectData withKillCredit(Optional<UUID> killCredit) {
        return new AbilityEffectData(dragTicks, shader, faceMaskTexture, isSpectating, spectationInterrupted, privateChat, allowRemoteDamage, killCredit);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.ABILITY_EFFECTS, this);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.ABILITY_EFFECTS, this), entity.getServer());
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
