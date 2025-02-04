package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ServerboundJumpMidSwingingPayload() implements ExtendedPacketPayload {
    public static final Type<ServerboundJumpMidSwingingPayload> TYPE = new Type<>(Mineraculous.modLoc("jump_mid_swinging"));
    public static final ServerboundJumpMidSwingingPayload INSTANCE = new ServerboundJumpMidSwingingPayload();
    public static final StreamCodec<ByteBuf, ServerboundJumpMidSwingingPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handle(Player player) {
        Optional<Integer> id = player.getData(MineraculousAttachmentTypes.LADYBUG_YOYO);
        if (id.isPresent()) {
            Level level = player.level();
            if (level instanceof ServerLevel serverLevel && serverLevel.getEntity(id.get()) instanceof ThrownLadybugYoyo thrownLadybugYoyo) {
                Vec3 fromProjectileToPlayer = new Vec3(player.getX() - thrownLadybugYoyo.getX(), player.getY() - thrownLadybugYoyo.getY(), player.getZ() - thrownLadybugYoyo.getZ());
                double distance = fromProjectileToPlayer.length();
                if (thrownLadybugYoyo.inGround() && !player.isNoGravity() && !player.onGround() && !player.getAbilities().flying && distance >= thrownLadybugYoyo.getServerMaxRopeLength() - 0.2) {
                    player.addDeltaMovement(new Vec3(0, 1.2, 0));
                    player.hurtMarked = true;
                    thrownLadybugYoyo.recall();
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
