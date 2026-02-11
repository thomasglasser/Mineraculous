package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FakeMiraculousItem extends AbstractMiraculousItem {
    public FakeMiraculousItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide()) {
            if (stack.has(MineraculousDataComponents.POWER_STATE) && stack.get(MineraculousDataComponents.POWER_STATE) != AbstractMiraculousItem.PowerState.POWERED) {
                stack.set(MineraculousDataComponents.POWER_STATE, AbstractMiraculousItem.PowerState.POWERED);
            }
        }
    }
}
