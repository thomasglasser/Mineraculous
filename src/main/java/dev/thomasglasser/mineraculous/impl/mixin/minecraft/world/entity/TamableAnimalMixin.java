package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.entity;

import dev.thomasglasser.mineraculous.impl.world.entity.TameableEntity;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin extends Animal implements TameableEntity {
    private TamableAnimalMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    @Nullable
    public abstract UUID getOwnerUUID();

    @Shadow
    public abstract void setOwnerUUID(@Nullable UUID uuid);
}
