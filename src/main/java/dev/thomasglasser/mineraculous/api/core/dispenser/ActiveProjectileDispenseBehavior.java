package dev.thomasglasser.mineraculous.api.core.dispenser;

import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ActiveProjectileDispenseBehavior extends ProjectileDispenseBehavior {
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

    public ActiveProjectileDispenseBehavior(Item projectile) {
        super(projectile);
    }

    @Override
    public ItemStack execute(BlockSource blockSource, ItemStack item) {
        if (Active.isActive(item))
            return super.execute(blockSource, item);
        return defaultDispenseItemBehavior.dispense(blockSource, item);
    }
}
