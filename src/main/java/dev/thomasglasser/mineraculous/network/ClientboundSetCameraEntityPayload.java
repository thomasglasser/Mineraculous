package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSetCameraEntityPayload(int entityId, Optional<ResourceLocation> shader, Optional<String> toggleTag, boolean tagTarget, boolean overrideActive, Optional<ResourceKey<Miraculous>> miraculous) implements ExtendedPacketPayload {

    public static final Type<ClientboundSetCameraEntityPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_set_camera_entity"));
    public static final StreamCodec<ByteBuf, ClientboundSetCameraEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSetCameraEntityPayload::entityId,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ClientboundSetCameraEntityPayload::shader,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ClientboundSetCameraEntityPayload::toggleTag,
            ByteBufCodecs.BOOL, ClientboundSetCameraEntityPayload::tagTarget,
            ByteBufCodecs.BOOL, ClientboundSetCameraEntityPayload::overrideActive,
            ByteBufCodecs.optional(ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS)), ClientboundSetCameraEntityPayload::miraculous,
            ClientboundSetCameraEntityPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);
        Entity entity = player.level().getEntity(entityId);
        if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown()) {
            if (overrideActive && miraculous.isPresent()) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetPowerActivatedPayload(miraculous.get(), false, false));
            }
            if (entity == null || entityData.getBoolean(MineraculousEntityEvents.TAG_CAMERA_CONTROL_INTERRUPTED)) {
                MineraculousClientUtils.setCameraEntity(null);
                toggleTag.ifPresent(tag -> {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(tag, false));
                    if (tagTarget && entity != null)
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(Optional.of(entity.getId()), tag, false));
                });
            } else {
                if (entityData.getInt(MineraculousEntityEvents.TAG_WAITTICKS) <= 6) {
                    MineraculousKeyMappings.ACTIVATE_POWER.get().setDown(false);
                    if (MineraculousClientUtils.getCameraEntity() == entity) {
                        MineraculousClientUtils.setCameraEntity(null);
                    } else {
                        MineraculousClientUtils.setCameraEntity(entity);
                        shader.ifPresent(MineraculousClientUtils::setShader);
                    }
                    toggleTag.ifPresent(tag -> {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(tag, MineraculousClientUtils.getCameraEntity() != null));
                        if (tagTarget)
                            TommyLibServices.NETWORK.sendToServer(new ServerboundSetToggleTagPayload(Optional.of(entity.getId()), tag, MineraculousClientUtils.getCameraEntity() != null));
                    });
                } else {
                    entityData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, entityData.getInt(MineraculousEntityEvents.TAG_WAITTICKS) - 1);
                    TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
