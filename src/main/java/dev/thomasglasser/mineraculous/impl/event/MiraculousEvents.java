package dev.thomasglasser.mineraculous.impl.event;

import dev.thomasglasser.mineraculous.api.event.MiraculousEvent;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import net.minecraft.world.entity.LivingEntity;

public class MiraculousEvents {
    public static boolean isPowered(LivingEntity entity) {
        return entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed();
    }

    public static boolean hasTransformationFrames(LivingEntity entity) {
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).flatMap(KamikotizationData::transformationState).isPresent())
            return true;
        for (MiraculousData data : entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).values()) {
            if (data.transformationState().isPresent())
                return true;
        }
        return false;
    }

    public static void onTriggerTransformMiraculous(MiraculousEvent.Transform.Trigger event) {
        LivingEntity entity = event.getEntity();
        if (isPowered(entity) || hasTransformationFrames(entity)) {
            event.setCanceled(true);
        }
    }

    public static void onPreTransformMiraculous(MiraculousEvent.Transform.Pre event) {
        LivingEntity entity = event.getEntity();
        if (isPowered(entity) || hasTransformationFrames(entity)) {
            event.setCanceled(true);
        }
    }

    public static void onPreDetransformMiraculous(MiraculousEvent.Detransform.Pre event) {
        LivingEntity entity = event.getEntity();
        if (hasTransformationFrames(entity))
            event.setCanceled(true);
    }
}
