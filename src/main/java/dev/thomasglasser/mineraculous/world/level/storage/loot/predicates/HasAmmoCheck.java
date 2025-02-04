package dev.thomasglasser.mineraculous.world.level.storage.loot.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.level.storage.loot.parameters.MineraculousLootContextParams;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record HasAmmoCheck(boolean hasAmmo) implements LootItemCondition {
    public static final MapCodec<HasAmmoCheck> CODEC = RecordCodecBuilder.mapCodec(
            p_345271_ -> p_345271_.group(Codec.BOOL.fieldOf("has_ammo").forGetter(HasAmmoCheck::hasAmmo)).apply(p_345271_, HasAmmoCheck::new));

    public boolean test(LootContext context) {
        Boolean hasAmmo = context.getParamOrNull(MineraculousLootContextParams.HAS_AMMO);
        return hasAmmo != null && hasAmmo == this.hasAmmo;
    }

    @Override
    public LootItemConditionType getType() {
        return MineraculousLootItemConditions.HAS_AMMO.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(MineraculousLootContextParams.HAS_AMMO);
    }

    public static LootItemCondition.Builder hasAmmoCheck() {
        return () -> new HasAmmoCheck(true);
    }

    public static LootItemCondition.Builder hasNoAmmoCheck() {
        return () -> new HasAmmoCheck(false);
    }
}
