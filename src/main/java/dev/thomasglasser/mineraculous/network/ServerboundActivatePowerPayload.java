package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public record ServerboundActivatePowerPayload(ResourceKey<Miraculous> miraculousType) implements ExtendedPacketPayload {
    public static final Type<ServerboundActivatePowerPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_activate_main_power"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundActivatePowerPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundActivatePowerPayload::miraculousType,
            ServerboundActivatePowerPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
        MiraculousData data = miraculousDataSet.get(miraculousType);
        data.miraculousItem().set(MineraculousDataComponents.REMAINING_TICKS.get(), MiraculousItem.FIVE_MINUTES);
        miraculousDataSet.put(player, miraculousType, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, true, data.name(), data.look()), true);
        CuriosUtils.setStackInSlot(player, data.curiosData(), data.miraculousItem(), true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
