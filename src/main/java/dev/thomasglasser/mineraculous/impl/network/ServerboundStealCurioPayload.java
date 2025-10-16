package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record ServerboundStealCurioPayload(UUID target, CuriosData data) implements ExtendedPacketPayload {
    public static final Type<ServerboundStealCurioPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_steal_curio"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStealCurioPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundStealCurioPayload::target,
            CuriosData.STREAM_CODEC, ServerboundStealCurioPayload::data,
            ServerboundStealCurioPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(this.target);
        if (target != null) {
            ItemStack stack = CuriosUtils.getStackInSlot(target, this.data);
            Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null && stack.has(MineraculousDataComponents.POWERED)) {
                target.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).detransform(target, (ServerLevel) player.level(), miraculous, stack, true);
            }
            if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || stack.has(MineraculousDataComponents.KAMIKOTIZING)) {
                player.displayClientMessage(ExternalInventoryScreen.ITEM_BOUND_KEY, true);
            } else {
                CuriosUtils.setStackInSlot(target, this.data, ItemStack.EMPTY);
                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                } else {
                    player.drop(stack, true, true);
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
