package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownLadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ClientboundCalculateYoyoRenderLengthPayload(int yoyoID, int pID) implements ExtendedPacketPayload {
    public static final Type<ClientboundCalculateYoyoRenderLengthPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_calculate_yoyo_render_length"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCalculateYoyoRenderLengthPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundCalculateYoyoRenderLengthPayload::yoyoID,
            ByteBufCodecs.INT, ClientboundCalculateYoyoRenderLengthPayload::pID,
            ClientboundCalculateYoyoRenderLengthPayload::new);

    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(yoyoID);
        Player p = (Player) player.level().getEntity(pID); //ladybug
        if (entity instanceof ThrownLadybugYoyo thrownYoyo) {
            Vec3 vec3;
            if (p == Minecraft.getInstance().player) {
                vec3 = MineraculousClientUtils.getFirstPersonHandPosition(true, false, ThrownLadybugYoyoRenderer.RIGHT_SCALE, ThrownLadybugYoyoRenderer.UP_SCALE);
                Vec3 fromProjectileToHand = new Vec3(vec3.x - thrownYoyo.getX(), vec3.y - thrownYoyo.getY(), vec3.z - thrownYoyo.getZ());
                thrownYoyo.setFirstPovRenderMaxRopeLength((float) fromProjectileToHand.length());
            }
            vec3 = MineraculousClientUtils.getHumanoidEntityHandPos(p, true, 0.15f, -0.75, 0);
            Vec3 fromProjectileToHand = new Vec3(vec3.x - thrownYoyo.getX(), vec3.y - thrownYoyo.getY(), vec3.z - thrownYoyo.getZ());
            thrownYoyo.setRenderMaxRopeLength((float) fromProjectileToHand.length());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
