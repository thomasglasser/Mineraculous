package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ServerboundUpdateYoyoLengthPayload(boolean increase) implements ExtendedPacketPayload {
    public static final Type<ServerboundUpdateYoyoLengthPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_update_yoyo_length"));
    public static final StreamCodec<ByteBuf, ServerboundUpdateYoyoLengthPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundUpdateYoyoLengthPayload::increase,
            ServerboundUpdateYoyoLengthPayload::new);

    @Override
    public void handle(Player player) {
        ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(player.level());
        if (thrownYoyo != null) {
            float maxRopeLn = thrownYoyo.getMaxRopeLength();
            Vec3 fromProjectileToPlayer = new Vec3(player.getX() - thrownYoyo.getX(), player.getY() - thrownYoyo.getY(), player.getZ() - thrownYoyo.getZ());
            double distance = fromProjectileToPlayer.length();
            if (increase) {
                if (distance >= maxRopeLn - 0.2) {
                    thrownYoyo.setMaxRopeLength(thrownYoyo.getMaxRopeLength() + 0.3f);
                    TommyLibServices.NETWORK.sendToAllClients(new ClientboundCalculateYoyoRenderLengthPayload(thrownYoyo.getId(), player.getId()), player.getServer());
                }
            } else if (distance > 2) {
                thrownYoyo.setMaxRopeLength(thrownYoyo.getMaxRopeLength() - 0.2f);
                TommyLibServices.NETWORK.sendToAllClients(new ClientboundCalculateYoyoRenderLengthPayload(thrownYoyo.getId(), player.getId()), player.getServer());
            }
        } else {
            player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).ifPresent(leashingData -> leashingData.withMaxRopeLength(leashingData.maxRopeLength() + (increase ? 0.5f : -0.5f)).save(player, true));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
