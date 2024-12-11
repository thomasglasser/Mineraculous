package dev.thomasglasser.mineraculous.data.blockstates;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.blockstates.ExtendedBlockStateProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import java.util.SortedMap;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousBlockStateProvider extends ExtendedBlockStateProvider {
    public MineraculousBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Mineraculous.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(MineraculousBlocks.CATACLYSM_BLOCK.get());

        getVariantBuilder(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).get()).forAllStates(blockState -> {
            CheeseBlock.Age age = CheeseBlock.Age.FRESH;
            String name = "cheese";
            int bites = blockState.getValue(CheeseBlock.BITES);
            String suffix = bites > 0 ? "_slice" + bites : "";
            String ageName = age.getSerializedName();
            return ConfiguredModel.builder()
                    .modelFile(models()
                            .withExistingParent(MineraculousBlocks.CHEESE_BLOCKS.get(CheeseBlock.Age.FRESH).getId().getPath() + suffix, modBlockLoc("cheese" + suffix + "_base"))
                            .texture("inner", modBlockLoc("cheese/" + ageName + "_" + name + "_inner"))
                            .texture("side", modBlockLoc("cheese/" + ageName + "_" + name + "_side"))
                            .texture("top", modBlockLoc("cheese/" + ageName + "_" + name + "_top"))
                            .texture("bottom", modBlockLoc("cheese/" + ageName + "_" + name + "_bottom"))
                            .texture("particle", modBlockLoc("cheese/" + ageName + "_" + name + "_side")))
                    .build();
        });

        // TODO: Textures for all
//		cheese(MineraculousBlocks.CHEESE_BLOCKS, "cheese");
//		cheese(MineraculousBlocks.WAXED_CHEESE_BLOCKS, "waxed_cheese");
//		cheese(MineraculousBlocks.CAMEMBERT_BLOCKS, "camembert");
//		cheese(MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS, "waxed_camembert");

        getVariantBuilder(MineraculousBlocks.HIBISCUS_BUSH.get()).forAllStates(state -> {
            int stage = state.getValue(SweetBerryBushBlock.AGE);
            return ConfiguredModel.builder()
                    .modelFile(models().withExistingParent(MineraculousBlocks.HIBISCUS_BUSH.getId().getPath() + "_stage" + stage, "block/cross")
                            .texture("cross", modBlockLoc(MineraculousBlocks.HIBISCUS_BUSH.getId().getPath() + "_stage" + stage))
                            .renderType("cutout"))
                    .build();
        });
    }

    protected void cheese(SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> map, String name) {
        map.forEach(((age, block) -> getVariantBuilder(block.get()).forAllStates(blockState -> {
            int bites = blockState.getValue(CheeseBlock.BITES);
            String suffix = bites > 0 ? "_slice" + bites : "";
            String ageName = age.getSerializedName();
            return ConfiguredModel.builder()
                    .modelFile(models()
                            .withExistingParent(block.getId().getPath() + suffix, modBlockLoc("cheese" + suffix + "_base"))
                            .texture("inner", modBlockLoc("cheese/" + ageName + "_" + name + "_inner"))
                            .texture("side", modBlockLoc("cheese/" + ageName + "_" + name + "_side"))
                            .texture("top", modBlockLoc("cheese/" + ageName + "_" + name + "_top"))
                            .texture("bottom", modBlockLoc("cheese/" + ageName + "_" + name + "_bottom"))
                            .texture("particle", modBlockLoc("cheese/" + ageName + "_" + name + "_side")))
                    .build();
        })));
    }
}
