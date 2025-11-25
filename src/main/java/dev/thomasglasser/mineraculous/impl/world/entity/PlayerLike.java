package dev.thomasglasser.mineraculous.impl.world.entity;

import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

public interface PlayerLike {
    static AttributeSupplier.Builder createDefaultAttributes() {
        return Player.createAttributes()
                .add(Attributes.FOLLOW_RANGE);
    }

    AttributeMap getAttributes();

    @Nullable
    CompoundTag getShoulderEntityLeft();

    @Nullable
    CompoundTag getShoulderEntityRight();

    PlayerSkin getSkin();

    boolean isModelPartShown(PlayerModelPart part);

    double xCloakO();

    double yCloakO();

    double zCloakO();

    double xCloak();

    double yCloak();

    double zCloak();

    float oBob();

    float bob();

    Scoreboard getScoreboard();

    Vec3 getDeltaMovementLerped(float partialTick);
}
