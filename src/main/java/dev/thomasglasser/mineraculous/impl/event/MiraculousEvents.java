package dev.thomasglasser.mineraculous.impl.event;

import dev.thomasglasser.mineraculous.api.event.MiraculousEvent;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import net.minecraft.world.entity.LivingEntity;

public class MiraculousEvents {
    public static void onPreTransformMiraculous(MiraculousEvent.Transform.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            event.setCanceled(true);
        }
    }
}
