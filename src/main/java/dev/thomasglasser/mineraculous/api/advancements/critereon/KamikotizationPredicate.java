package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import java.util.Optional;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Predicate for entity {@link Kamikotization} according to their {@link KamikotizationData}.
 * 
 * @param kamikotizations Matching {@link Kamikotization}s
 */
public record KamikotizationPredicate(Optional<HolderSet<Kamikotization>> kamikotizations) implements EntitySubPredicate {
    public static final MapCodec<KamikotizationPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("kamikotizations").forGetter(KamikotizationPredicate::kamikotizations)).apply(instance, KamikotizationPredicate::new));

    /**
     * Creates a kamikotization predicate for any kamikotization.
     * 
     * @return The kamikotization predicate
     */
    public static KamikotizationPredicate any() {
        return new KamikotizationPredicate(Optional.empty());
    }

    /**
     * Creates a kamikotization predicate for no kamikotization.
     * 
     * @return The kamikotization predicate
     */
    public static KamikotizationPredicate none() {
        return new KamikotizationPredicate(Optional.of(HolderSet.empty()));
    }

    /**
     * Creates a kamikotization predicate for the provided kamikotization set.
     * 
     * @param kamikotizations Matching {@link Kamikotization}s
     * @return The kamikotization predicate
     */
    public static KamikotizationPredicate kamikotizations(HolderSet<Kamikotization> kamikotizations) {
        return new KamikotizationPredicate(Optional.of(kamikotizations));
    }

    /**
     * Creates a kamikotization predicate for the provided kamikotization.
     * 
     * @param kamikotization Matching {@link Kamikotization}
     * @return The kamikotization predicate
     */
    public static KamikotizationPredicate kamikotization(Holder<Kamikotization> kamikotization) {
        return kamikotizations(HolderSet.direct(kamikotization));
    }

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return MineraculousEntitySubPredicates.KAMIKOTIZATION.get();
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 pos) {
        Optional<Holder<Kamikotization>> kamikotization = entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization);
        if (this.kamikotizations.isPresent()) {
            HolderSet<Kamikotization> kamikotizations = this.kamikotizations.get();
            if (kamikotizations.size() == 0)
                return kamikotization.isEmpty();
            return kamikotization.map(kamikotizations::contains).orElse(false);
        }
        return kamikotization.isPresent();
    }
}
