package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public record ServerboundSetLadybugYoyoAbilityPayload(InteractionHand hand, String selected) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetLadybugYoyoAbilityPayload> TYPE = new Type<>(Mineraculous.modLoc("set_ladybug_yoyo_ability"));
    public static final StreamCodec<ByteBuf, ServerboundSetLadybugYoyoAbilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(InteractionHand::valueOf, InteractionHand::name), ServerboundSetLadybugYoyoAbilityPayload::hand,
            ByteBufCodecs.STRING_UTF8, ServerboundSetLadybugYoyoAbilityPayload::selected,
            ServerboundSetLadybugYoyoAbilityPayload::new);

    @Override
    public void handle(Player player) {
        ItemStack item = player.getItemInHand(hand);
        LadybugYoyoItem.Ability previous = item.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY);
        LadybugYoyoItem.Ability current = LadybugYoyoItem.Ability.valueOf(selected);
        item.set(MineraculousDataComponents.LADYBUG_YOYO_ABILITY, current);
        if (current == LadybugYoyoItem.Ability.PURIFY && previous != LadybugYoyoItem.Ability.PURIFY)
            ((SingletonGeoAnimatable) item.getItem()).triggerAnim(player, GeoItem.getOrAssignId(item, (ServerLevel) player.level()), LadybugYoyoItem.CONTROLLER_OPEN, LadybugYoyoItem.ANIMATION_OPEN);
        else if (current != LadybugYoyoItem.Ability.PURIFY && previous == LadybugYoyoItem.Ability.PURIFY)
            ((SingletonGeoAnimatable) item.getItem()).triggerAnim(player, GeoItem.getOrAssignId(item, (ServerLevel) player.level()), LadybugYoyoItem.CONTROLLER_OPEN, LadybugYoyoItem.ANIMATION_CLOSE);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
