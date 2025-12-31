package dev.thomasglasser.mineraculous.impl.event;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.event.KamikotizationEvent;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class KamikotizationEvents {
    public static void onPreTransformKamikotization(KamikotizationEvent.Transform.Pre event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getStack();

        if (stack != null)
            stack.remove(MineraculousDataComponents.KAMIKOTIZING);

        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || MiraculousEvents.hasTransformationFrames(entity)) {
            event.setCanceled(true);
        }
    }

    public static void onPreDetransformKamikotization(KamikotizationEvent.Detransform.Pre event) {
        LivingEntity entity = event.getEntity();
        if (MiraculousEvents.hasTransformationFrames(entity))
            event.setCanceled(true);
    }
}
