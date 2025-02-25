package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetCatStaffAbilityPayload(InteractionHand hand, CatStaffItem.Ability selected) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetCatStaffAbilityPayload> TYPE = new Type<>(Mineraculous.modLoc("set_cat_staff_ability"));
    public static final StreamCodec<ByteBuf, ServerboundSetCatStaffAbilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(InteractionHand::valueOf, InteractionHand::name), ServerboundSetCatStaffAbilityPayload::hand,
            CatStaffItem.Ability.STREAM_CODEC, ServerboundSetCatStaffAbilityPayload::selected,
            ServerboundSetCatStaffAbilityPayload::new);

    @Override
    public void handle(Player player) {
        player.getItemInHand(hand).set(MineraculousDataComponents.CAT_STAFF_ABILITY, selected);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
