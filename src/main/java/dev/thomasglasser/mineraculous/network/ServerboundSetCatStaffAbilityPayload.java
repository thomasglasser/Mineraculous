package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetCatStaffAbilityPayload(int slot, String selected) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetCatStaffAbilityPayload> TYPE = new Type<>(Mineraculous.modLoc("set_cat_staff_ability"));
    public static final StreamCodec<ByteBuf, ServerboundSetCatStaffAbilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetCatStaffAbilityPayload::slot,
            ByteBufCodecs.STRING_UTF8, ServerboundSetCatStaffAbilityPayload::selected,
            ServerboundSetCatStaffAbilityPayload::new);

    @Override
    public void handle(Player player) {
        player.getInventory().getItem(slot).set(MineraculousDataComponents.CAT_STAFF_ABILITY, CatStaffItem.Ability.valueOf(selected));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
