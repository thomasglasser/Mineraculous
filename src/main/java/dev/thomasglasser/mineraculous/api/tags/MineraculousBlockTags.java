package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.tags.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MineraculousBlockTags {
    /// Blocks that are unable to be replaced by {@link Abilities#CATACLYSM}.
    public static final TagKey<Block> CATACLYSM_IMMUNE = create("cataclysm_immune");

    // Cheeses
    /// Blocks from any mod that can be considered a cheese block and food.
    public static final TagKey<Block> CHEESE_BLOCKS_FOODS = createC("foods/cheese_blocks");

    /// Blocks that are normal cheese.
    public static final TagKey<Block> CHEESE_BLOCKS = create("cheese_blocks");
    /// Blocks that are camembert cheese.
    public static final TagKey<Block> CAMEMBERT_BLOCKS = create("camembert_blocks");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, Mineraculous.modLoc(name));
    }

    private static TagKey<Block> createC(String name) {
        return TagUtils.createConventional(Registries.BLOCK, name);
    }
}
