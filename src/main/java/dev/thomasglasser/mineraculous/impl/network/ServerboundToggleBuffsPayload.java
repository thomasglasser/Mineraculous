package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class ServerboundToggleBuffsPayload implements ExtendedPacketPayload {
    public static final ServerboundToggleBuffsPayload INSTANCE = new ServerboundToggleBuffsPayload();
    public static final Type<ServerboundToggleBuffsPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_toggle_buffs"));
    public static final StreamCodec<ByteBuf, ServerboundToggleBuffsPayload> CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundToggleBuffsPayload() {}

    @Override
    public void handle(Player player) {
        List<Holder<Miraculous>> transformed = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed();
        if (!transformed.isEmpty()) {
            Holder<Miraculous> miraculous = transformed.getFirst();
            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
            if (data.transformed()) {
                player.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, miraculousEffect) -> {
                    if (miraculousEffect.toggleable()) {
                        player.removeEffect(player.level().holderOrThrow(effect));
                    }
                });
                data.toggleBuffsActive().save(miraculous, player, true);
            }
        } else {
            Optional<KamikotizationData> data = player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION);
            if (data.isPresent()) {
                player.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, miraculousEffect) -> {
                    if (miraculousEffect.toggleable()) {
                        player.removeEffect(player.level().holderOrThrow(effect));
                    }
                });
                data.get().toggleBuffsActive().save(player);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
