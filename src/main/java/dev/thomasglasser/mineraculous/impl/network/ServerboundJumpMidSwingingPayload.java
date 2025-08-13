package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record ServerboundJumpMidSwingingPayload() implements ExtendedPacketPayload {
    public static final ServerboundJumpMidSwingingPayload INSTANCE = new ServerboundJumpMidSwingingPayload();
    public static final Type<ServerboundJumpMidSwingingPayload> TYPE = new Type<>(Mineraculous.modLoc("jump_mid_swinging"));
    public static final StreamCodec<ByteBuf, ServerboundJumpMidSwingingPayload> CODEC = StreamCodec.unit(INSTANCE);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Level level = player.level();
        ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(level);
        if (thrownYoyo != null) {
            Vec3 fromProjectileToPlayer = new Vec3(player.getX() - thrownYoyo.getX(), player.getY() - thrownYoyo.getY(), player.getZ() - thrownYoyo.getZ());
            double distance = fromProjectileToPlayer.length();
            if (thrownYoyo.inGround() && !player.isNoGravity() && !player.onGround() && !player.getAbilities().flying && distance >= thrownYoyo.getMaxRopeLength() - 0.2) {
                if (!level.getBlockState(new BlockPos((int) player.getX(), (int) player.getY() - 1, (int) player.getZ())).isSolid()) {
                    player.addDeltaMovement(new Vec3(0, 1.2, 0));
                    player.hurtMarked = true;
                    data.startSafeFall().save(player, true);
                    thrownYoyo.recall();
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
