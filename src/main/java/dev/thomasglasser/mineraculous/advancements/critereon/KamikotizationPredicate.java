package dev.thomasglasser.mineraculous.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record KamikotizationPredicate(HolderSet<Kamikotization> kamikotizations) implements EntitySubPredicate {
    public static final KamikotizationPredicate ANY = new KamikotizationPredicate(HolderSet.empty());

    public static final MapCodec<KamikotizationPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("kamikotizations", HolderSet.empty()).forGetter(KamikotizationPredicate::kamikotizations)).apply(instance, KamikotizationPredicate::new));

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return MineraculousEntitySubPredicates.KAMIKOTIZATION.get();
    }

    @Override
    public boolean matches(Entity entity, ServerLevel serverLevel, @Nullable Vec3 vec3) {
        if (entity instanceof LivingEntity livingEntity && livingEntity.hasData(MineraculousAttachmentTypes.KAMIKOTIZATION)) {
            if (kamikotizations instanceof HolderSet.Direct<Kamikotization> && kamikotizations.size() == 0) {
                return true;
            } else {
                KamikotizationData data = livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION);
                return data.kamikotization().isPresent() && kamikotizations.contains(serverLevel.registryAccess().holderOrThrow(data.kamikotization().get()));
            }
        }
        return false;
    }
}
