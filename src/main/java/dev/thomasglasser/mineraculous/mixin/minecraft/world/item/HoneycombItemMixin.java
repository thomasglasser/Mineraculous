package dev.thomasglasser.mineraculous.mixin.minecraft.world.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import java.util.Optional;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
    @ModifyReturnValue(method = "getWaxed", at = @At("RETURN"))
    private static Optional<BlockState> getWaxed(Optional<BlockState> result, BlockState original) {
        if (original.getBlock() instanceof CheeseBlock cheeseBlock && !cheeseBlock.isWaxed()) {
            return Optional.of(cheeseBlock.getWaxedBlock().get().withPropertiesOf(original));
        }
        return result;
    }
}
