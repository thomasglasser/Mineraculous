package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffPerchHandler;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record ServerboundUpdateStaffPerchLength(PerchingCatStaffData perchingData, boolean ascend, boolean descend) implements ExtendedPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundUpdateStaffPerchLength> TYPE = new CustomPacketPayload.Type<>(Mineraculous.modLoc("staff_perch_update_length"));
    public static final StreamCodec<ByteBuf, ServerboundUpdateStaffPerchLength> CODEC = StreamCodec.composite(
            PerchingCatStaffData.STREAM_CODEC, ServerboundUpdateStaffPerchLength::perchingData,
            ByteBufCodecs.BOOL, ServerboundUpdateStaffPerchLength::ascend,
            ByteBufCodecs.BOOL, ServerboundUpdateStaffPerchLength::descend,
            ServerboundUpdateStaffPerchLength::new);
    @Override
    public void handle(Player player) {
        float groundRYClient = perchingData.yGroundLevel();
        float length = perchingData.length();
        int perchTickClient = perchingData.tick();
        boolean upOrDownKeyPressed = ascend || descend;

        float d = 0;
        boolean shouldNotFall = (groundRYClient == length);
        if (descend) {
            d -= 0.3f;
        }
        if (ascend && Math.abs(groundRYClient) < MineraculousServerConfig.get().maxToolLength.get()) {
            d += 0.3f;
        }
        if (perchTickClient >= CatStaffPerchHandler.MAX_TICKS) {
            if (!upOrDownKeyPressed && shouldNotFall) d = 0;
            Vec3 vec3 = new Vec3(player.getDeltaMovement().x, d, player.getDeltaMovement().z);
            player.hurtMarked = true;
            player.setDeltaMovement(vec3);
            player.resetFallDistance();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
