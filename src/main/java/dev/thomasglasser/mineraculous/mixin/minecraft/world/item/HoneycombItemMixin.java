package dev.thomasglasser.mineraculous.mixin.minecraft.world.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin
{
	@ModifyReturnValue(method = "getWaxed", at = @At("RETURN"))
	private static Optional<BlockState> getWaxed(Optional<BlockState> result, BlockState original)
	{
		if (original.getBlock() instanceof CheeseBlock cheeseBlock)
		{
			return Optional.of(CheeseBlock.WAXABLES.get().get(cheeseBlock).defaultBlockState());
		}
		return result;
	}
}
