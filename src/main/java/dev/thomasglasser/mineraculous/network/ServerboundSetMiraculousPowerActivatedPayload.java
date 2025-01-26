package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
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
import net.minecraft.world.level.Level;

public record ServerboundSetMiraculousPowerActivatedPayload(ResourceKey<Miraculous> miraculousType) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetMiraculousPowerActivatedPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_miraculous_power_activated"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetMiraculousPowerActivatedPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundSetMiraculousPowerActivatedPayload::miraculousType,
            ServerboundSetMiraculousPowerActivatedPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
        MiraculousData data = miraculousDataSet.get(miraculousType);
        data = data.withPowerStatus(true, true);
        if (data.shouldCountDown())
            data.miraculousItem().set(MineraculousDataComponents.REMAINING_TICKS.get(), MiraculousItem.FIVE_MINUTES);
        Level level = player.level();
        Ability power = level.registryAccess().holderOrThrow(miraculousType).value().activeAbility().get().value();
        power.playStartSound(level, player.blockPosition());
        miraculousDataSet.put(player, miraculousType, data, true);
        CuriosUtils.setStackInSlot(player, data.curiosData(), data.miraculousItem(), true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
