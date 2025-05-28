package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
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

public record ServerboundSetButterflyCaneAbilityPayload(InteractionHand hand, ButterflyCaneItem.Ability selected) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetButterflyCaneAbilityPayload> TYPE = new Type<>(Mineraculous.modLoc("set_butterfly_cane_ability"));
    public static final StreamCodec<ByteBuf, ServerboundSetButterflyCaneAbilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(InteractionHand::valueOf, InteractionHand::name), ServerboundSetButterflyCaneAbilityPayload::hand,
            ButterflyCaneItem.Ability.STREAM_CODEC, ServerboundSetButterflyCaneAbilityPayload::selected,
            ServerboundSetButterflyCaneAbilityPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack stack = player.getItemInHand(hand);
        ButterflyCaneItem.Ability old = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY);
        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY, selected);
        if (old != selected) {
            ServerLevel serverlevel = (ServerLevel) player.level();
            MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            MiraculousData storingData = miraculousesData.get(miraculousesData.getFirstKeyIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE, serverlevel));
            if (old == ButterflyCaneItem.Ability.BLADE)
                ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, serverlevel), ButterflyCaneItem.CONTROLLER_USE, ButterflyCaneItem.ANIMATION_SHEATHE);
            else if (old == ButterflyCaneItem.Ability.KAMIKO_STORE && storingData != null && !storingData.extraData().contains(ButterflyCaneItem.TAG_STORED_KAMIKO))
                ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, serverlevel), ButterflyCaneItem.CONTROLLER_USE, ButterflyCaneItem.ANIMATION_CLOSE);
            if (selected == ButterflyCaneItem.Ability.BLADE)
                ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, serverlevel), ButterflyCaneItem.CONTROLLER_USE, ButterflyCaneItem.ANIMATION_UNSHEATHE);
            else if (selected == ButterflyCaneItem.Ability.KAMIKO_STORE && storingData != null && !storingData.extraData().contains(ButterflyCaneItem.TAG_STORED_KAMIKO))
                ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, serverlevel), ButterflyCaneItem.CONTROLLER_USE, ButterflyCaneItem.ANIMATION_OPEN);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
