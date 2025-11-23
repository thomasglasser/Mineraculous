package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetFaceMaskTexturePayload(int targetID, Optional<ResourceLocation> texture) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetFaceMaskTexturePayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_clear_face_mask_texture"));
    public static final StreamCodec<ByteBuf, ServerboundSetFaceMaskTexturePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetFaceMaskTexturePayload::targetID,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), ServerboundSetFaceMaskTexturePayload::texture,
            ServerboundSetFaceMaskTexturePayload::new);

    @Override
    public void handle(Player player) {
        Entity target = player.level().getEntity(targetID);
        if (target != null) {
            target.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withFaceMaskTexture(texture).save(target);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
