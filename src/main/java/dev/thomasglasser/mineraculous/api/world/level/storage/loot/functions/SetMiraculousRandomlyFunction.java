package dev.thomasglasser.mineraculous.api.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetMiraculousRandomlyFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetMiraculousRandomlyFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance)
            .apply(instance, SetMiraculousRandomlyFunction::new));

    protected SetMiraculousRandomlyFunction(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    public LootItemFunctionType<? extends SetMiraculousRandomlyFunction> getType() {
        return MineraculousLootItemFunctionTypes.SET_MIRACULOUS_RANDOMLY.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        List<Holder.Reference<Miraculous>> miraculouses = context.getLevel()
                .holderLookup(MineraculousRegistries.MIRACULOUS)
                .listElements()
                .toList();
        Optional<Holder.Reference<Miraculous>> optional = Util.getRandomSafe(miraculouses, random);
        if (optional.isEmpty()) {
            MineraculousConstants.LOGGER.warn("Couldn't find a compatible miraculous data for {}", stack);
            return stack;
        } else {
            stack.set(MineraculousDataComponents.MIRACULOUS, optional.get());
            return stack;
        }
    }

    public static Builder randomMiraculous() {
        return new Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        protected Builder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetMiraculousRandomlyFunction(this.getConditions());
        }
    }
}
