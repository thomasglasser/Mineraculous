package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Predicate for current entity {@link Kamikotization} according to their {@link KamikotizationData}.
 * 
 * @param kamikotizations Matching {@link Kamikotization}s
 */
public record KamikotizationPredicate(HolderSet<Kamikotization> kamikotizations) implements EntitySubPredicate {
    public static final KamikotizationPredicate ANY = new KamikotizationPredicate(HolderSet.empty());

    public static final MapCodec<KamikotizationPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("kamikotizations", HolderSet.empty()).forGetter(KamikotizationPredicate::kamikotizations)).apply(instance, KamikotizationPredicate::new));

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return MineraculousEntitySubPredicates.KAMIKOTIZATION.get();
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 pos) {
        Optional<KamikotizationData> kamikotizationData = entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION);
        if (kamikotizationData.isPresent()) {
            if (kamikotizations == HolderSet.<Kamikotization>empty()) {
                return true;
            } else {
                return kamikotizations.contains(kamikotizationData.get().kamikotization());
            }
        }
        return false;
    }
}
