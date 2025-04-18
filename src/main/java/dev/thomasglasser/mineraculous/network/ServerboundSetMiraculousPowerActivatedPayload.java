package dev.thomasglasser.mineraculous.network;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetMiraculousPowerActivatedPayload(ResourceKey<Miraculous> miraculousType) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetMiraculousPowerActivatedPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_miraculous_power_activated"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetMiraculousPowerActivatedPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundSetMiraculousPowerActivatedPayload::miraculousType,
            ServerboundSetMiraculousPowerActivatedPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        Ability power = level.registryAccess().holderOrThrow(miraculousType).value().activeAbility().get().value();
        MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
        MiraculousData data = miraculousDataSet.get(miraculousType);
        if (power.canActivate(new AbilityData(data.powerLevel(), Either.left(miraculousType)), level, player.blockPosition(), player)) {
            power.playStartSound(level, player.blockPosition());
            data = data.withPowerStatus(true, true);
            if (data.shouldCountDown())
                data.miraculousItem().set(MineraculousDataComponents.REMAINING_TICKS.get(), MiraculousItem.FIVE_MINUTES);
            miraculousDataSet.put(player, miraculousType, data, true);
            CuriosUtils.setStackInSlot(player, data.curiosData(), data.miraculousItem());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
