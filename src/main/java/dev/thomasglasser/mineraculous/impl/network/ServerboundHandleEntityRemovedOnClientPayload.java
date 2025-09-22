package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public record ServerboundHandleEntityRemovedOnClientPayload(int entityId) implements ExtendedPacketPayload {
    public static final Type<ServerboundHandleEntityRemovedOnClientPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_handle_entity_removed_on_client"));
    public static final StreamCodec<ByteBuf, ServerboundHandleEntityRemovedOnClientPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundHandleEntityRemovedOnClientPayload::entityId,
            ServerboundHandleEntityRemovedOnClientPayload::new);

    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
            int leashedId = player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).get().leashedId();
            if (leashedId == entityId)
                LadybugYoyoItem.removeLeash(entity, player);
        }
        if (entity instanceof ThrownLadybugYoyo thrownYoyo)
            if (player == thrownYoyo.getPlayerOwner()) {
                thrownYoyo.discard();
            }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
