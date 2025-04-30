package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetToggleTagPayload(Optional<Integer> target, String tag, boolean show) implements ExtendedPacketPayload {

    public static final Type<ServerboundSetToggleTagPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_toggle_tag"));
    public static final StreamCodec<ByteBuf, ServerboundSetToggleTagPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT), ServerboundSetToggleTagPayload::target,
            ByteBufCodecs.STRING_UTF8, ServerboundSetToggleTagPayload::tag,
            ByteBufCodecs.BOOL, ServerboundSetToggleTagPayload::show,
            ServerboundSetToggleTagPayload::new);
    public ServerboundSetToggleTagPayload(String tag, boolean show) {
        this(Optional.empty(), tag, show);
    }

    // ON SERVER
    @Override
    public void handle(Player player) {
        Entity entity = target.isPresent() ? player.level().getEntity(target.get()) : player;
        // TODO: Fix
//        CompoundTag compoundTag = TommyLibServices.ENTITY.getPersistentData(entity);
//        compoundTag.putBoolean(tag, show);
//        compoundTag.putBoolean(MineraculousEntityEvents.TAG_CAMERA_CONTROL_INTERRUPTED, false);
//        compoundTag.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
//        TommyLibServices.ENTITY.setPersistentData(entity, compoundTag, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
