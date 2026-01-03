package dev.thomasglasser.mineraculous.impl.world.entity;

import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface TamableEntity extends OwnableEntity {
    void setOwnerUUID(@Nullable UUID ownerId);

    @Override
    Level level();

    @Override
    @Nullable
    default LivingEntity getOwner() {
        UUID uuid = this.getOwnerUUID();
        return uuid == null ? null : this.level().getEntities().get(uuid) instanceof LivingEntity living ? living : null;
    }
}
