package dev.thomasglasser.mineraculous.data.blockstates;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.blockstates.ExtendedBlockStateProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import java.util.SortedMap;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousBlockStateProvider extends ExtendedBlockStateProvider {
    public MineraculousBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Mineraculous.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(MineraculousBlocks.CATACLYSM_BLOCK.get());

        cheese(MineraculousBlocks.CHEESE_BLOCKS, MineraculousBlocks.WAXED_CHEESE_BLOCKS, "cheese");
        cheese(MineraculousBlocks.CAMEMBERT_BLOCKS, MineraculousBlocks.WAXED_CAMEMBERT_BLOCKS, "camembert");

        simpleBlock(MineraculousBlocks.CHEESE_POT.get(), models().getExistingFile(blockLoc(MineraculousBlocks.CHEESE_POT)));

        getVariantBuilder(MineraculousBlocks.HIBISCUS_BUSH.get()).forAllStates(state -> {
            int stage = state.getValue(SweetBerryBushBlock.AGE);
            return ConfiguredModel.builder()
                    .modelFile(models().withExistingParent(MineraculousBlocks.HIBISCUS_BUSH.getId().getPath() + "_stage" + stage, "block/cross")
                            .texture("cross", modBlockLoc(MineraculousBlocks.HIBISCUS_BUSH.getId().getPath() + "_stage" + stage))
                            .renderType("cutout"))
                    .build();
        });
    }

    protected void cheese(SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> blocks, SortedMap<CheeseBlock.Age, DeferredBlock<CheeseBlock>> waxed, String name) {
        Table<CheeseBlock.Age, Integer, ModelFile> models = HashBasedTable.create();
        blocks.forEach(((age, block) -> getVariantBuilder(block.get()).forAllStates(blockState -> {
            int bites = blockState.getValue(CheeseBlock.BITES);
            String suffix = bites > 0 ? "_slice" + bites : "";
            String ageName = age.getSerializedName();
            BlockModelBuilder model = models()
                    .withExistingParent(block.getId().getPath() + suffix, modBlockLoc("cheese" + suffix + "_base"))
                    .texture("inner", modBlockLoc("cheese/" + ageName + "_" + name + "_inner"))
                    .texture("side", modBlockLoc("cheese/" + ageName + "_" + name + "_side"))
                    .texture("top", modBlockLoc("cheese/" + ageName + "_" + name + "_top"))
                    .texture("bottom", modBlockLoc("cheese/" + ageName + "_" + name + "_bottom"))
                    .texture("particle", modBlockLoc("cheese/" + ageName + "_" + name + "_side"));
            models.put(age, bites, model);
            return ConfiguredModel.builder()
                    .rotationY((int) (blockState.getValue(CheeseBlock.FACING).getOpposite()).toYRot())
                    .modelFile(model)
                    .build();
        })));
        waxed.forEach(((age, block) -> getVariantBuilder(block.get()).forAllStates(blockState -> {
            int bites = blockState.getValue(CheeseBlock.BITES);
            return ConfiguredModel.builder()
                    .rotationY((int) (blockState.getValue(CheeseBlock.FACING).getOpposite()).toYRot())
                    .modelFile(models.get(age, bites))
                    .build();
        })));
    }
}
