package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record ServerboundStealCuriosPayload(UUID target, CuriosData data) implements ExtendedPacketPayload {
    public static final Type<ServerboundStealCuriosPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_steal_curios"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStealCuriosPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundStealCuriosPayload::target,
            CuriosData.STREAM_CODEC, ServerboundStealCuriosPayload::data,
            ServerboundStealCuriosPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(this.target);
        if (target != null) {
            ItemStack stack = CuriosUtils.getStackInSlot(target, this.data);
            ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null && stack.has(MineraculousDataComponents.POWERED)) {
                MineraculousEntityEvents.handleMiraculousTransformation((ServerPlayer) target, miraculous, target.getData(MineraculousAttachmentTypes.MIRACULOUS).get(miraculous), false, true, false);
                MineraculousEntityEvents.renounceMiraculous(stack, (ServerLevel) player.level());
            }
            if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                player.displayClientMessage(Component.translatable(ExternalInventoryScreen.ITEM_BOUND_KEY), true);
            } else {
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                CuriosUtils.setStackInSlot(target, this.data, ItemStack.EMPTY, true);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
