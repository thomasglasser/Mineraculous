package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import java.util.Optional;

import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Predicate for entity cat staff use according to their {@link TravelingCatStaffData} and {@link dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData}.
 * 
 * @param traveling Whether the entity must be traveling
 * @param perching  Whether the entity must be perching
 */
public record CatStaffPredicate(Optional<Boolean> traveling, Optional<Boolean> perching) implements EntitySubPredicate {
    public static final MapCodec<CatStaffPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("traveling").forGetter(CatStaffPredicate::traveling),
            Codec.BOOL.optionalFieldOf("perching").forGetter(CatStaffPredicate::perching)).apply(instance, CatStaffPredicate::new));

    /**
     * Creates a cat staff predicate for any cat staff use.
     * 
     * @return The cat staff predicate
     */
    public static CatStaffPredicate using() {
        return new CatStaffPredicate(Optional.empty(), Optional.empty());
    }

    /**
     * Creates a cat staff predicate for traveling status.
     * 
     * @param traveling Whether the entity must be traveling
     * @return The cat staff predicate
     */
    public static CatStaffPredicate traveling(boolean traveling) {
        return new CatStaffPredicate(Optional.of(traveling), Optional.empty());
    }

    /**
     * Creates a cat staff predicate for perching status.
     * 
     * @param perching Whether the entity must be perching
     * @return The cat staff predicate
     */
    public static CatStaffPredicate perching(boolean perching) {
        return new CatStaffPredicate(Optional.empty(), Optional.of(perching));
    }

    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return MineraculousEntitySubPredicates.CAT_STAFF.get();
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        return traveling.map(traveling -> traveling == entity.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF).isModeActive()).orElse(true) &&
                perching.map(perching -> perching == entity.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF).isModeActive()).orElse(true);
    }
}
