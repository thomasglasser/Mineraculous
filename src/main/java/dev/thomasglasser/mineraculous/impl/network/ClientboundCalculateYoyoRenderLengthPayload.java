package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ClientboundCalculateYoyoRenderLengthPayload(int yoyoId, int holderId) implements ExtendedPacketPayload {
    public static final Type<ClientboundCalculateYoyoRenderLengthPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_calculate_yoyo_render_length"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCalculateYoyoRenderLengthPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundCalculateYoyoRenderLengthPayload::yoyoId,
            ByteBufCodecs.INT, ClientboundCalculateYoyoRenderLengthPayload::holderId,
            ClientboundCalculateYoyoRenderLengthPayload::new);

    @Override
    public void handle(Player player) {
        if (player.level().getEntity(yoyoId) instanceof ThrownLadybugYoyo yoyo) {
            Entity holder = player.level().getEntity(holderId);
            Vec3 vec3;
            if (holder == ClientUtils.getLocalPlayer()) {
                vec3 = MineraculousClientUtils.getFirstPersonHandPosition(true, false, ThrownLadybugYoyoRenderer.RIGHT_SCALE, ThrownLadybugYoyoRenderer.UP_SCALE);
                Vec3 fromProjectileToHand = new Vec3(vec3.x - yoyo.getX(), vec3.y - yoyo.getY(), vec3.z - yoyo.getZ());
                yoyo.setFirstPovRenderMaxRopeLength((float) fromProjectileToHand.length());
            }
            vec3 = MineraculousClientUtils.getHumanoidEntityHandPos(holder, true, 0.15f, -0.75, 0);
            Vec3 fromProjectileToHand = new Vec3(vec3.x - yoyo.getX(), vec3.y - yoyo.getY(), vec3.z - yoyo.getZ());
            yoyo.setRenderMaxRopeLength((float) fromProjectileToHand.length());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
