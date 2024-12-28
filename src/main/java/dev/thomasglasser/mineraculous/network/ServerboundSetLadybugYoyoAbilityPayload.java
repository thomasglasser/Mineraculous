package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundSetLadybugYoyoAbilityPayload(int slot, String selected) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetLadybugYoyoAbilityPayload> TYPE = new Type<>(Mineraculous.modLoc("set_ladybug_yoyo_ability"));
    public static final StreamCodec<ByteBuf, ServerboundSetLadybugYoyoAbilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetLadybugYoyoAbilityPayload::slot,
            ByteBufCodecs.STRING_UTF8, ServerboundSetLadybugYoyoAbilityPayload::selected,
            ServerboundSetLadybugYoyoAbilityPayload::new);

    @Override
    public void handle(Player player) {
        ItemStack item = player.getInventory().getItem(slot);
        LadybugYoyoItem.Ability current = LadybugYoyoItem.Ability.valueOf(selected);
        item.set(MineraculousDataComponents.LADYBUG_YOYO_ABILITY, current);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
