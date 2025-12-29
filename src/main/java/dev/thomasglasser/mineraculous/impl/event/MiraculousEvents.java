package dev.thomasglasser.mineraculous.impl.event;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.event.MiraculousEvent;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class MiraculousEvents {
    public static void onCanEquipMiraculous(MiraculousEvent.CanEquip event) {
        Holder<Miraculous> miraculous = event.getMiraculous();
        CuriosData curiosData = event.getCuriosData();
        if (event.canEquip())
            event.setCanEquip(curiosData.identifier().equals(miraculous.value().acceptableSlot()));
    }

    public static void onEquipMiraculous(MiraculousEvent.Equip event) {
        LivingEntity entity = event.getEntity();
        Holder<Miraculous> miraculous = event.getMiraculous();
        MiraculousData data = event.getMiraculousData();
        ItemStack stack = event.getStack();
        if (!entity.level().isClientSide() && !data.transformed()) {
            UUID miraculousId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
            if (miraculousId == null) {
                miraculousId = UUID.randomUUID();
                stack.set(MineraculousDataComponents.MIRACULOUS_ID, miraculousId);
            }
            if (stack.has(MineraculousDataComponents.POWERED)) {
                stack.remove(MineraculousDataComponents.POWERED);
                Kwami kwami = MineraculousEntityUtils.summonKwami(entity, stack.getOrDefault(MineraculousDataComponents.CHARGED, true), miraculousId, miraculous, true, null);
                if (kwami != null) {
                    stack.set(MineraculousDataComponents.KWAMI_ID, kwami.getUUID());
                } else {
                    MineraculousConstants.LOGGER.error("Kwami could not be created for entity {}", entity.getName().plainCopy().getString());
                }
            }
        }
    }

    public static void onPreTransformMiraculous(MiraculousEvent.Transform.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
            event.setCanceled(true);
        }
    }
}
