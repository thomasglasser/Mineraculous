package dev.thomasglasser.mineraculous.api.world.level.storage.loot.functions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class DyeRandomlyFunction extends LootItemConditionalFunction {
    public static final MapCodec<DyeRandomlyFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> commonFields(instance).and(
            instance.group(NumberProviders.CODEC.optionalFieldOf("color_count", ConstantValue.exactly(1)).forGetter(function -> function.colorCount),
                    Codec.BOOL.optionalFieldOf("only_dyeable", true).forGetter(function -> function.onlyDyeable)))
            .apply(instance, DyeRandomlyFunction::new));
    private final NumberProvider colorCount;
    private final boolean onlyDyeable;

    protected DyeRandomlyFunction(List<LootItemCondition> predicates, NumberProvider colorCount, boolean onlyDyeable) {
        super(predicates);
        this.colorCount = colorCount;
        this.onlyDyeable = onlyDyeable;
    }

    @Override
    public LootItemFunctionType<? extends DyeRandomlyFunction> getType() {
        return MineraculousLootItemFunctionTypes.DYE_RANDOMLY.get();
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        RandomSource random = context.getRandom();
        List<DyeItem> dyes = new ReferenceArrayList<>();
        int count = this.colorCount.getInt(context);
        for (int i = 0; i < count; i++) {
            dyes.add(DyeItem.byColor(DyeColor.byId(random.nextInt(DyeColor.values().length))));
        }
        return onlyDyeable ? DyedItemColor.applyDyes(stack, dyes) : MineraculousItemUtils.applyDyesToUndyeable(stack, dyes);
    }

    public static DyeRandomlyFunction.Builder randomDye() {
        return new DyeRandomlyFunction.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<Builder> {
        private NumberProvider colorCount = ConstantValue.exactly(1);
        private boolean onlyDyeable = true;

        protected Builder getThis() {
            return this;
        }

        public Builder withColorCount(NumberProvider colorCount) {
            this.colorCount = colorCount;
            return this;
        }

        public Builder allowingUndyeableItems() {
            this.onlyDyeable = false;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new DyeRandomlyFunction(this.getConditions(), colorCount, onlyDyeable);
        }
    }
}
