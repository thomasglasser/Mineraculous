package dev.thomasglasser.mineraculous.api.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import java.util.Optional;
import net.minecraft.advancements.critereon.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

/**
 * Predicate for item's {@link MineraculousDataComponents#LUCKY_CHARM} component.
 * 
 * @param self Whether the target must be the owner of the lucky charm
 */
public record ItemLuckyCharmPredicate(Optional<Boolean> self) implements SingleComponentItemPredicate<LuckyCharm> {
    public static final Codec<ItemLuckyCharmPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("self").forGetter(ItemLuckyCharmPredicate::self)).apply(instance, ItemLuckyCharmPredicate::new));

    /**
     * Creates a lucky charm predicate for any lucky charm.
     * 
     * @return The lucky charm predicate
     */
    public static ItemLuckyCharmPredicate any() {
        return new ItemLuckyCharmPredicate(Optional.empty());
    }

    /**
     * Creates a lucky charm predicate for the provided self value.
     * 
     * @param self Whether the target must be the owner of the lucky charm
     * @return The lucky charm predicate
     */
    public static ItemLuckyCharmPredicate self(boolean self) {
        return new ItemLuckyCharmPredicate(Optional.of(self));
    }

    @Override
    public DataComponentType<LuckyCharm> componentType() {
        return MineraculousDataComponents.LUCKY_CHARM.get();
    }

    @Override
    public boolean matches(ItemStack stack, LuckyCharm value) {
        return self.map(self -> self == value.owner().equals(value.target().orElse(null))).orElse(true);
    }
}
