package dev.thomasglasser.mineraculous.api.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/**
 * Specifies a block to be replaced with upon aging to the next stage in {@link AgeingCheese}.
 * 
 * @param next The block to be replaced with
 */
public record Ageable(Block next) {
    public static final Codec<Ageable> AGEABLE_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Ageable::new, Ageable::next);
    public static final Codec<Ageable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(instance -> instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("next").forGetter(Ageable::next)).apply(instance, Ageable::new)),
            AGEABLE_CODEC);
}
