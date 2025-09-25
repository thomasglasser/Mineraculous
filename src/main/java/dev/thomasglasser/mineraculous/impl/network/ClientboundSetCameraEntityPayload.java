package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ClientboundSetCameraEntityPayload(Optional<Integer> entityId, Optional<ResourceLocation> shader) implements ExtendedPacketPayload {
    public static final Type<ClientboundSetCameraEntityPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_set_camera_entity"));
    public static final StreamCodec<ByteBuf, ClientboundSetCameraEntityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT), ClientboundSetCameraEntityPayload::entityId,
            TommyLibExtraStreamCodecs.OPTIONAL_RESOURCE_LOCATION, ClientboundSetCameraEntityPayload::shader,
            ClientboundSetCameraEntityPayload::new);

    public ClientboundSetCameraEntityPayload(Optional<Integer> entityId) {
        this(entityId, Optional.empty());
    }

    // ON CLIENT
    @Override
    public void handle(Player player) {
        entityId.ifPresentOrElse(id -> {
            Entity entity = player.level().getEntity(id);
            MineraculousClientUtils.setCameraEntity(entity);
            if (entity != null) {
                MineraculousClientUtils.setShader(shader.orElse(null));
            }
        }, () -> MineraculousClientUtils.setCameraEntity(null));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
