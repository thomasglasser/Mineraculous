package dev.thomasglasser.mineraculous.impl.event;

import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.datamaps.LuckyCharms;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.api.event.LuckyCharmEvent;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LuckyCharmEvents {
    public static void onDetermineLuckyCharmSpawnPos(LuckyCharmEvent.DetermineSpawnPos event) {
        LivingEntity performer = event.getEntity();
        Level level = performer.level();
        ItemStack tool = event.getTool();
        if (tool != null && tool.getItem() instanceof LadybugYoyoItem) {
            ThrownLadybugYoyoData yoyoData = performer.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
            if (yoyoData.getThrownYoyo(level) instanceof ThrownLadybugYoyo yoyo) {
                if (performer.position().distanceTo(yoyo.position()) > 20 ||
                        yoyo.inGround() ||
                        performer.getXRot() > -70)
                    event.setCanceled(true);
                else {
                    yoyo.setDeltaMovement(Vec3.ZERO);
                    yoyoData.setSummonedLuckyCharm(true).save(performer);
                    event.setSpawnPos(yoyo.position());
                }
            }
        }
    }

    public static void onDetermineLuckyCharms(LuckyCharmEvent.DetermineLuckyCharms event) {
        Level level = event.getEntity().level();
        Entity target = event.getTarget();
        if (target != null) {
            Optional<Holder<Kamikotization>> kamikotization = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization);
            if (kamikotization.isPresent()) {
                LuckyCharms luckyCharms = level.registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).getData(MineraculousDataMaps.KAMIKOTIZATION_LUCKY_CHARMS, kamikotization.get().getKey());
                if (luckyCharms != null) {
                    event.setLuckyCharms(luckyCharms);
                }
            } else {
                MiraculousesData miraculousesData = target.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                if (miraculousesData.isTransformed()) {
                    Holder<Miraculous> miraculous = miraculousesData.getTransformed().getFirst();
                    LuckyCharms luckyCharms = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getData(MineraculousDataMaps.MIRACULOUS_LUCKY_CHARMS, miraculous.getKey());
                    if (luckyCharms != null) {
                        event.setLuckyCharms(luckyCharms);
                    }
                }
            }
        }
    }
}
