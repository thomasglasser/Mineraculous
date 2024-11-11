package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSetCameraEntityPayload(int entityId) implements ExtendedPacketPayload {
    public static final Type<ClientboundSetCameraEntityPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_set_camera_entity"));
    public static final StreamCodec<ByteBuf, ClientboundSetCameraEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSetCameraEntityPayload::entityId,
            ClientboundSetCameraEntityPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);
        Entity entity = player.level().getEntity(entityId);
        if (entity != null) {
            if (entityData.getInt(MineraculousEntityEvents.TAG_WAITTICKS) <= 0 && MineraculousKeyMappings.ACTIVATE_POWER.isDown()) {
                if (MineraculousClientUtils.getCameraEntity() == entity) {
                    MineraculousClientUtils.setCameraEntity(null);
                } else {
                    MineraculousClientUtils.setCameraEntity(entity);
                }
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetShowKamikoMaskPayload(MineraculousClientUtils.getCameraEntity() != null));
                entityData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
