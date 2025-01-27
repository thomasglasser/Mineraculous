package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public record ClientboundToggleNightVisionPayload(boolean nightVision, Optional<ResourceLocation> shader) implements ExtendedPacketPayload {
    public static final Type<ClientboundToggleNightVisionPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_toggle_cat_vision"));
    public static final StreamCodec<ByteBuf, ClientboundToggleNightVisionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundToggleNightVisionPayload::nightVision,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ClientboundToggleNightVisionPayload::shader,
            ClientboundToggleNightVisionPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        if (nightVision) {
            shader.ifPresent(MineraculousClientUtils::setShader);
            player.addEffect(MineraculousEntityEvents.INFINITE_HIDDEN_EFFECT.apply(MobEffects.NIGHT_VISION, 0));
        } else {
            if (shader.isPresent())
                MineraculousClientUtils.setShader(null);
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
        CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(player);
        tag.putBoolean(MineraculousEntityEvents.TAG_HAS_NIGHT_VISION, nightVision);
        TommyLibServices.ENTITY.setPersistentData(player, tag, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
