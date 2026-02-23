package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ModeTool;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundSetMiraculousToolModePayload(ItemStack stack, String modeId) implements ExtendedPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundSetMiraculousToolModePayload> TYPE = new CustomPacketPayload.Type<>(MineraculousConstants.modLoc("serverbound_miraculous_tool_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetMiraculousToolModePayload> CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ServerboundSetMiraculousToolModePayload::stack,
            ByteBufCodecs.stringUtf8(1000), ServerboundSetMiraculousToolModePayload::modeId,
            ServerboundSetMiraculousToolModePayload::new);

    @Override
    public void handle(Player player) {
        ItemStack itemStack = player.getInventory().getSelected();
        InteractionHand hand;
        if (stack == player.getMainHandItem()) {
            hand = InteractionHand.MAIN_HAND;
        } else {
            hand = InteractionHand.OFF_HAND;
        }
        if (stack.getItem() instanceof ModeTool<?> tool)
            tool.getAllowedToolModes(stack, player).stream()
                    .filter(m -> m.getSerializedName().equals(modeId))
                    .findFirst()
                    .ifPresent(mode -> {
                        applyMode(tool, stack, hand, player, mode);
                    });
    }

    @SuppressWarnings("unchecked")
    private <M extends ModeTool.ToolMode> void applyMode(ModeTool<M> tool, ItemStack stack, InteractionHand hand, Player player, ModeTool.ToolMode mode) {
        tool.setToolMode(stack, (M) mode);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
