package dev.thomasglasser.mineraculous.mixin.mineraculous.world.level.block;

import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CheeseBlock.class)
public abstract class CheeseBlockMixin implements IBlockExtension
{
	@Override
	public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate)
	{
		if (toolAction == ToolActions.AXE_WAX_OFF)
		{
			return CheeseBlock.WAX_OFF_BY_BLOCK.get().get(state.getBlock()).defaultBlockState();
		}
		return IBlockExtension.super.getToolModifiedState(state, context, toolAction, simulate);
	}
}
