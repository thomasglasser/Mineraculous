package dev.thomasglasser.mineraculous.mixin.mineraculous.world.level.block;

import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CheeseBlock.class)
public abstract class CheeseBlockMixin extends Block
{
	private CheeseBlockMixin(Properties p_49795_)
	{
		super(p_49795_);
	}

	@Override
	public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate)
	{
		Block block = CheeseBlock.WAX_OFF_BY_BLOCK.get().get(this);
		if (toolAction == ToolActions.AXE_WAX_OFF && block != null)
		{
			return block.defaultBlockState();
		}
		return super.getToolModifiedState(state, context, toolAction, simulate);
	}
}
