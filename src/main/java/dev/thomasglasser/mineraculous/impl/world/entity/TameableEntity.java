package dev.thomasglasser.mineraculous.impl.world.entity;

import java.util.UUID;
import net.minecraft.world.entity.OwnableEntity;
import org.jetbrains.annotations.Nullable;

public interface TameableEntity extends OwnableEntity {
    void setOwnerUUID(@Nullable UUID ownerId);
}
