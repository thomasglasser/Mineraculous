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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record ServerboundSetPowerActivatedPayload(ResourceKey<Miraculous> miraculousType, boolean activated, boolean active) implements ExtendedPacketPayload {

    public static final Type<ServerboundSetPowerActivatedPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_activate_main_power"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetPowerActivatedPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), ServerboundSetPowerActivatedPayload::miraculousType,
            ByteBufCodecs.BOOL, ServerboundSetPowerActivatedPayload::activated,
            ByteBufCodecs.BOOL, ServerboundSetPowerActivatedPayload::active,
            ServerboundSetPowerActivatedPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
        MiraculousData data = miraculousDataSet.get(miraculousType);
        if (activated) {
            if (data.hasLimitedPower())
                data.miraculousItem().set(MineraculousDataComponents.REMAINING_TICKS.get(), MiraculousItem.FIVE_MINUTES);
            Level level = player.level();
            Ability power = level.registryAccess().holderOrThrow(miraculousType).value().activeAbility().get().value();
            power.playStartSound(level, player.blockPosition());
        }
        miraculousDataSet.put(player, miraculousType, data.withPowerStatus(activated, active), true);
        CuriosUtils.setStackInSlot(player, data.curiosData(), data.miraculousItem(), true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
