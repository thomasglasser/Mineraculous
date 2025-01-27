package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSetCameraEntityPayload(int entityId, Optional<ResourceLocation> shader, Optional<String> toggleTag, boolean tagTarget, boolean overrideOwner) implements ExtendedPacketPayload {

    public static final Type<ClientboundSetCameraEntityPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_set_camera_entity"));
    public static final StreamCodec<ByteBuf, ClientboundSetCameraEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSetCameraEntityPayload::entityId,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ClientboundSetCameraEntityPayload::shader,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ClientboundSetCameraEntityPayload::toggleTag,
            ByteBufCodecs.BOOL, ClientboundSetCameraEntityPayload::tagTarget,
            ByteBufCodecs.BOOL, ClientboundSetCameraEntityPayload::overrideOwner,
            ClientboundSetCameraEntityPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);
        Entity entity = player.level().getEntity(entityId);
        if (entity == null || entityData.getBoolean(MineraculousEntityEvents.TAG_CAMERA_CONTROL_INTERRUPTED)) {
            MineraculousClientUtils.setCameraEntity(null);
            toggleTag.ifPresent(tag -> {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(tag, false));
                if (tagTarget && entity != null)
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(Optional.of(entity.getId()), tag, false));
            });
        } else if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown()) {
            if (entityData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS) <= 6) {
                MineraculousKeyMappings.ACTIVATE_POWER.get().setDown(false);
                if (MineraculousClientUtils.getCameraEntity() == entity) {
                    MineraculousClientUtils.setCameraEntity(null);
                } else {
                    MineraculousClientUtils.setCameraEntity(entity);
                    shader.ifPresent(MineraculousClientUtils::setShader);
                    if (overrideOwner)
                        TommyLibServices.NETWORK.sendToServer(new ServerboundTameEntityPayload(entity.getId()));
                }
                toggleTag.ifPresent(tag -> {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(tag, MineraculousClientUtils.isCameraEntityOther()));
                    if (tagTarget)
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(Optional.of(entity.getId()), tag, MineraculousClientUtils.isCameraEntityOther()));
                });
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
