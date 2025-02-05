package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.tags.MineraculousMiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public record ServerboundSetButterflyCaneAbilityPayload(int slot, String selected) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetButterflyCaneAbilityPayload> TYPE = new Type<>(Mineraculous.modLoc("set_butterfly_cane_ability"));
    public static final StreamCodec<ByteBuf, ServerboundSetButterflyCaneAbilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetButterflyCaneAbilityPayload::slot,
            ByteBufCodecs.STRING_UTF8, ServerboundSetButterflyCaneAbilityPayload::selected,
            ServerboundSetButterflyCaneAbilityPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack stack = player.getInventory().getItem(slot);
        ButterflyCaneItem.Ability old = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY);
        ButterflyCaneItem.Ability current = ButterflyCaneItem.Ability.valueOf(selected);
        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY, current);
        if (old != current) {
            ServerLevel serverlevel = (ServerLevel) player.level();
            MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
            MiraculousData storingData = miraculousDataSet.get(miraculousDataSet.getFirstKeyIn(MineraculousMiraculousTags.CAN_USE_BUTTERFLY_CANE, serverlevel));
            if (old == ButterflyCaneItem.Ability.BLADE)
                ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, serverlevel), ButterflyCaneItem.CONTROLLER_USE, ButterflyCaneItem.ANIMATION_SHEATHE);
            else if (old == ButterflyCaneItem.Ability.KAMIKO_STORE && storingData != null && !storingData.extraData().contains(ButterflyCaneItem.TAG_STORED_KAMIKO))
                ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, serverlevel), ButterflyCaneItem.CONTROLLER_USE, ButterflyCaneItem.ANIMATION_CLOSE);
            if (current == ButterflyCaneItem.Ability.BLADE)
                ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, serverlevel), ButterflyCaneItem.CONTROLLER_USE, ButterflyCaneItem.ANIMATION_UNSHEATHE);
            else if (current == ButterflyCaneItem.Ability.KAMIKO_STORE && storingData != null && !storingData.extraData().contains(ButterflyCaneItem.TAG_STORED_KAMIKO))
                ((SingletonGeoAnimatable) stack.getItem()).triggerAnim(player, GeoItem.getOrAssignId(stack, serverlevel), ButterflyCaneItem.CONTROLLER_USE, ButterflyCaneItem.ANIMATION_OPEN);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
