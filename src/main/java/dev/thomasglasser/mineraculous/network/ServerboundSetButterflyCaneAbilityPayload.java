package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetButterflyCaneAbilityPayload(int slot, String selected) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetButterflyCaneAbilityPayload> TYPE = new Type<>(Mineraculous.modLoc("set_butterfly_cane_ability"));
    public static final StreamCodec<ByteBuf, ServerboundSetButterflyCaneAbilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetButterflyCaneAbilityPayload::slot,
            ByteBufCodecs.STRING_UTF8, ServerboundSetButterflyCaneAbilityPayload::selected,
            ServerboundSetButterflyCaneAbilityPayload::new);

    @Override
    public void handle(Player player) {
        player.getInventory().getItem(slot).set(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY, ButterflyCaneItem.Ability.valueOf(selected));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
