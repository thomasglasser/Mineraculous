package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MineraculousBlockEntityWithoutLevelRenderer extends BlockEntityWithoutLevelRenderer {
    public MineraculousBlockEntityWithoutLevelRenderer() {
        super(null, null);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (stack.has(MineraculousDataComponents.MIRACULOUS)) {
            ResourceKey<Miraculous> type = stack.get(MineraculousDataComponents.MIRACULOUS);
            String prefix = "miraculous/";
            String basePath = prefix + type.location().getPath() + "/";
            if (stack.is(MineraculousItems.MIRACULOUS)) {
                String defaultHidden = basePath + "hidden";
                if (!stack.has(MineraculousDataComponents.POWERED.get())) {
                    if (stack.has(DataComponents.PROFILE) && stack.get(DataComponents.PROFILE).id().isPresent()) {
                        String look = ClientUtils.getPlayerByUUID(stack.get(DataComponents.PROFILE).id().get()).getData(MineraculousAttachmentTypes.MIRACULOUS).get(type).look();
                        if (!look.isEmpty())
                            ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, type.location().getNamespace(), basePath + look, defaultHidden);
                        else
                            ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, type.location().getNamespace(), defaultHidden);
                    } else {
                        ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, type.location().getNamespace(), defaultHidden);
                    }
                } else if (stack.has(MineraculousDataComponents.POWERED.get())) {
                    int ticks = stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0);
                    final int second = ticks / 20;
                    final int minute = (second / 60) + 1;
                    String defaultPowered = basePath + "powered";
                    if (ticks > 0 && ticks < MiraculousItem.FIVE_MINUTES) {
                        // Blinks every other second
                        if (second % 2 == 0)
                            ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, type.location().getNamespace(), basePath + "powered_" + (minute - 1));
                        // The first blink level should reference the normal powered model
                        else if (minute == 5)
                            ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, type.location().getNamespace(), defaultPowered);
                        else
                            ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, type.location().getNamespace(), basePath + "powered_" + minute);
                    } else {
                        ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, type.location().getNamespace(), defaultPowered);
                    }
                } else {
                    ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, type.location().getNamespace(), defaultHidden);
                }
            } else if (MineraculousArmors.MIRACULOUS.getAllAsItems().contains(stack.getItem())) {
                ClientUtils.renderItem(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, Mineraculous.MOD_ID, prefix + "armor");
            }
        }
        poseStack.popPose();
    }
}
