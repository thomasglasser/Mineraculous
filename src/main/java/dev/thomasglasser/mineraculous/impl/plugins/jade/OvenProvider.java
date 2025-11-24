package dev.thomasglasser.mineraculous.impl.plugins.jade;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.impl.world.level.block.entity.OvenBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.addon.vanilla.FurnaceProvider;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

public class OvenProvider implements IBlockComponentProvider, StreamServerDataProvider<BlockAccessor, FurnaceProvider.Data> {
    public static OvenProvider INSTANCE = new OvenProvider();

    private static final ResourceLocation ID = MineraculousConstants.modLoc("oven");

    private OvenProvider() {}

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        this.decodeFromData(accessor).ifPresent(data -> {
            tooltip.remove(JadeIds.MC_FURNACE);
            IElementHelper helper = IElementHelper.get();
            tooltip.add(helper.spacer(0, 0));
            for (int i : OvenBlockEntity.SLOTS_INPUT) {
                tooltip.append(helper.item(data.inventory().get(i)));
            }
            tooltip.append(helper.item(data.inventory().get(OvenBlockEntity.SLOT_FUEL)));
            tooltip.append(helper.spacer(4, 0));
            tooltip.append(helper.progress((float) data.progress() / (float) data.total()).translate(new Vec2(-2.0F, 0.0F)));
            tooltip.append(helper.item(data.inventory().get(OvenBlockEntity.SLOT_RESULT)));
        });
    }

    @Override
    public FurnaceProvider.Data streamData(BlockAccessor accessor) {
        OvenBlockEntity blockEntity = (OvenBlockEntity) accessor.getBlockEntity();
        return new FurnaceProvider.Data(blockEntity.getCookingProgress(), blockEntity.getCookingTotalTime(), blockEntity.getItemsView());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FurnaceProvider.Data> streamCodec() {
        return FurnaceProvider.Data.STREAM_CODEC;
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }
}
