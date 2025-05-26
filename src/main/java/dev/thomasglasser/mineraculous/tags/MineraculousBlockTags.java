package dev.thomasglasser.mineraculous.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.tags.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MineraculousBlockTags {
    public static final TagKey<Block> CATACLYSM_IMMUNE = create("cataclysm_immune");

    // Cheeses
    public static final TagKey<Block> CHEESE_BLOCKS_FOODS = createC("foods/cheese_blocks");

    public static final TagKey<Block> CHEESE_BLOCKS = create("cheese_blocks");
    public static final TagKey<Block> CAMEMBERT_BLOCKS = create("camembert_blocks");


    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, Mineraculous.modLoc(name));
    }

    private static TagKey<Block> createC(String name) {
        return TagUtils.createConventional(Registries.BLOCK, name);
    }
}
