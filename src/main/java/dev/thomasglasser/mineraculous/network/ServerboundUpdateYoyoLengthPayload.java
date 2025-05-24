package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
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
            float maxRopeLn = thrownYoyo.getServerMaxRopeLength();
            Vec3 fromProjectileToPlayer = new Vec3(player.getX() - thrownYoyo.getX(), player.getY() - thrownYoyo.getY(), player.getZ() - thrownYoyo.getZ());
            double distance = fromProjectileToPlayer.length();
            if (increase) {
                if (distance >= maxRopeLn - 0.2) {
                    thrownYoyo.setServerMaxRopeLength(thrownYoyo.getServerMaxRopeLength() + 0.3f);
                    thrownYoyo.updateRenderMaxRopeLength(player);
                }
            } else if (distance > 2) {
                thrownYoyo.setServerMaxRopeLength(thrownYoyo.getServerMaxRopeLength() - 0.2f);
                thrownYoyo.updateRenderMaxRopeLength(player);
            }
            System.out.println(thrownYoyo.getServerMaxRopeLength());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
