package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSetCameraEntityPayload(int entityId, Optional<ResourceLocation> shader, Optional<ResourceLocation> faceMaskTexture, boolean maskTarget, boolean overrideOwner) implements ExtendedPacketPayload {

    public static final Type<ClientboundSetCameraEntityPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_set_camera_entity"));
    public static final StreamCodec<ByteBuf, ClientboundSetCameraEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSetCameraEntityPayload::entityId,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ClientboundSetCameraEntityPayload::shader,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ClientboundSetCameraEntityPayload::faceMaskTexture,
            ByteBufCodecs.BOOL, ClientboundSetCameraEntityPayload::maskTarget,
            ByteBufCodecs.BOOL, ClientboundSetCameraEntityPayload::overrideOwner,
            ClientboundSetCameraEntityPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (MineraculousClientUtils.getCameraEntity() != player && (entity == null /*|| entityData.getBoolean(MineraculousEntityEvents.TAG_CAMERA_CONTROL_INTERRUPTED)*/)) {
            MineraculousClientUtils.setCameraEntity(null);
            AbilityEffectData.checkRemoveFaceMaskTexture(player, faceMaskTexture);
            if (maskTarget && entity != null) {
                AbilityEffectData.checkRemoveFaceMaskTexture(entity, faceMaskTexture);
            }
        } else if (MineraculousKeyMappings.ACTIVATE_POWER.isDown()) {
            // TODO: Implement ability key overriding
            MineraculousKeyMappings.ACTIVATE_POWER.setDown(false);
            if (MineraculousClientUtils.getCameraEntity() == entity) {
                MineraculousClientUtils.setCameraEntity(null);
            } else {
                MineraculousClientUtils.setCameraEntity(entity);
                shader.ifPresent(MineraculousClientUtils::setShader);
                if (overrideOwner)
                    TommyLibServices.NETWORK.sendToServer(new ServerboundTameEntityPayload(entity.getId()));
            }
            faceMaskTexture.ifPresent(texture -> {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetFaceMaskTexturePayload(player.getId(), MineraculousClientUtils.isCameraEntityOther() ? faceMaskTexture : Optional.empty()));
                if (maskTarget && entity != null)
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetFaceMaskTexturePayload(entity.getId(), MineraculousClientUtils.isCameraEntityOther() ? faceMaskTexture : Optional.empty()));
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
