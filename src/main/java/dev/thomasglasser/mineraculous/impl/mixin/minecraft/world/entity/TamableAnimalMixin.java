package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.entity;

import dev.thomasglasser.mineraculous.impl.world.entity.TamableEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin extends Animal implements TamableEntity {
    private TamableAnimalMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }
}
