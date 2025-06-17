package dev.thomasglasser.mineraculous.api.world.level.block;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.datamaps.Ageable;
import dev.thomasglasser.mineraculous.api.datamaps.MineraculousDataMaps;
import java.util.Optional;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface AgeingCheese extends ChangeOverTimeBlock<AgeingCheese.Age> {
    static Optional<Block> getNext(Block block) {
        Ageable ageable = block.builtInRegistryHolder().getData(MineraculousDataMaps.AGEABLES);
        return Optional.ofNullable(ageable).map(Ageable::next);
    }

    @Override
    default Optional<BlockState> getNext(BlockState state) {
        return getNext(state.getBlock()).map(block -> block.withPropertiesOf(state));
    }

    @Override
    default float getChanceModifier() {
        return this.getAge() == Age.FRESH ? 0.75F : 1.0F;
    }

    enum Age implements StringRepresentable {
        FRESH,
        AGED,
        RIPENED,
        EXQUISITE,
        TIME_HONORED;

        public static final Codec<Age> CODEC = StringRepresentable.fromEnum(Age::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public Age getNext() {
            if (ordinal() >= values().length - 1) return null;
            return values()[ordinal() + 1];
        }
    }
}
