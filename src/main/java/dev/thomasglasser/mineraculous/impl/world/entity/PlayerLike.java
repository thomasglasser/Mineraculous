package dev.thomasglasser.mineraculous.impl.world.entity;

import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

public interface PlayerLike {
    float BASE_MOVEMENT_SPEED = 0.3f;

    static AttributeSupplier.Builder createDefaultAttributes() {
        return Player.createAttributes()
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE);
    }

    static <T extends LivingEntity & PlayerLike> void adjustPlayerAttributes(T entity) {
        entity.getAttributes().getInstance(Attributes.MOVEMENT_SPEED).setBaseValue(BASE_MOVEMENT_SPEED);
    }

    AttributeMap getAttributes();

    @Nullable
    CompoundTag getShoulderEntityLeft();

    @Nullable
    CompoundTag getShoulderEntityRight();

    PlayerSkin getSkin();

    boolean isModelPartShown(PlayerModelPart part);

    Scoreboard getScoreboard();

    double xCloakO();

    double yCloakO();

    double zCloakO();

    double xCloak();

    double yCloak();

    double zCloak();

    float oBob();

    float bob();
}
