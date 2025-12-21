package dev.thomasglasser.mineraculous.impl.data.blockstates;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheeseEdibleFullBlock;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.block.PieceBlock;
import dev.thomasglasser.tommylib.api.data.blockstates.ExtendedBlockStateProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import java.util.Map;
import java.util.Objects;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousBlockStateProvider extends ExtendedBlockStateProvider {
    public MineraculousBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MineraculousConstants.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlock(MineraculousBlocks.CATACLYSM_BLOCK.get());

        cheese(MineraculousBlocks.CHEESE, MineraculousBlocks.WAXED_CHEESE, "cheese");
        cheese(MineraculousBlocks.CAMEMBERT, MineraculousBlocks.WAXED_CAMEMBERT, "camembert");

        simpleBlock(MineraculousBlocks.CHEESE_POT.get(), models().getExistingFile(blockLoc(MineraculousBlocks.CHEESE_POT)));
        ResourceLocation ovenSide = modBlockLoc("oven_side");
        ResourceLocation ovenBottom = modBlockLoc("oven_bottom");
        ResourceLocation ovenTop = modBlockLoc("oven_top");
        getVariantBuilder(MineraculousBlocks.OVEN.get()).forAllStates(state -> {
            boolean lit = state.getValue(FurnaceBlock.LIT);
            return ConfiguredModel.builder()
                    .modelFile(lit ? models().orientableWithBottom(MineraculousBlocks.OVEN.getId().getPath() + "_on", ovenSide, modBlockLoc("oven_front_on"), ovenBottom, ovenTop) : models().orientableWithBottom(MineraculousBlocks.OVEN.getId().getPath(), ovenSide, modBlockLoc("oven_front"), ovenBottom, ovenTop))
                    .rotationY((int) state.getValue(FurnaceBlock.FACING).getOpposite().toYRot())
                    .build();
        });

        getVariantBuilder(MineraculousBlocks.HIBISCUS_BUSH.get()).forAllStates(state -> {
            int stage = state.getValue(SweetBerryBushBlock.AGE);
            return ConfiguredModel.builder()
                    .modelFile(models().withExistingParent(MineraculousBlocks.HIBISCUS_BUSH.getId().getPath() + "_stage" + stage, "block/cross")
                            .texture("cross", modBlockLoc(MineraculousBlocks.HIBISCUS_BUSH.getId().getPath() + "_stage" + stage))
                            .renderType("cutout"))
                    .build();
        });
    }

    protected void cheese(Map<AgeingCheese.Age, DeferredBlock<AgeingCheeseEdibleFullBlock>> blocks, Map<AgeingCheese.Age, DeferredBlock<PieceBlock>> waxed, String name) {
        Table<AgeingCheese.Age, Integer, ModelFile> models = HashBasedTable.create();
        blocks.forEach(((age, block) -> getVariantBuilder(block.get()).forAllStates(blockState -> {
            int bites = blockState.getValue(block.get().getMissingPiecesProperty());
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
                    .modelFile(model)
                    .build();
        })));
        waxed.forEach(((age, block) -> getVariantBuilder(block.get()).forAllStates(blockState -> {
            int bites = blockState.getValue(block.get().getMissingPiecesProperty());
            return ConfiguredModel.builder()
                    .modelFile(Objects.requireNonNull(models.get(age, bites)))
                    .build();
        })));
    }
}
