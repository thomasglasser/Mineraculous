package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record ClientboundToggleNightVisionShaderPayload(boolean nightVision, ResourceLocation shader) implements ExtendedPacketPayload {
    public static final Type<ClientboundToggleNightVisionShaderPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_toggle_night_vision_shader"));
    public static final StreamCodec<ByteBuf, ClientboundToggleNightVisionShaderPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundToggleNightVisionShaderPayload::nightVision,
            ResourceLocation.STREAM_CODEC, ClientboundToggleNightVisionShaderPayload::shader,
            ClientboundToggleNightVisionShaderPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        MineraculousClientUtils.setShader(nightVision ? shader : null);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
