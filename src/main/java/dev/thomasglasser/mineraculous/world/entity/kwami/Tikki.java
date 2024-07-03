package dev.thomasglasser.mineraculous.world.entity.kwami;

import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Tikki extends Kwami {
    public Tikki(EntityType<? extends Tikki> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(MineraculousItemTags.TIKKI_FOODS);
    }

    @Override
    public boolean isTreat(ItemStack stack) {
        return stack.is(MineraculousItemTags.TIKKI_TREATS);
    }

    @Override
    public SoundEvent getHungrySound() {
        // TODO: Tikki hungry sound
        return null;
    }
}
