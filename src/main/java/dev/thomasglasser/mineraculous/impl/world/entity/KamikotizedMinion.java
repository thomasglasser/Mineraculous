package dev.thomasglasser.mineraculous.impl.world.entity;

import java.util.List;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import org.jetbrains.annotations.Nullable;

public class KamikotizedMinion extends TamableAnimal implements SmartBrainOwner<KamikotizedMinion>, PlayerLike {
    public KamikotizedMinion(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public List<? extends ExtendedSensor<? extends KamikotizedMinion>> getSensors() {
        return List.of();
    }

    public Player getPlayerOwner() {
        Entity entity = this.getOwner();
        if (entity instanceof Player player) {
            return player;
        } else {
            throw new IllegalStateException("KamikotizedMinion is not owned by a player!");
        }
    }

    @Override
    public Component getDisplayName() {
        return getPlayerOwner().getDisplayName();
    }

    @Override
    public AttributeMap getAttributes() {
        if (getOwner() instanceof Player player) {
            return player.getAttributes();
        }
        return super.getAttributes();
    }

    @Override
    public @Nullable CompoundTag getShoulderEntityLeft() {
        return getPlayerOwner().getShoulderEntityLeft();
    }

    @Override
    public @Nullable CompoundTag getShoulderEntityRight() {
        return getPlayerOwner().getShoulderEntityRight();
    }

    @Override
    public PlayerSkin getSkin() {
        if (getPlayerOwner() instanceof AbstractClientPlayer player)
            return player.getSkin();
        throw new IllegalStateException("KamikotizedMinion#getSkin() called on non-client player!");
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return getPlayerOwner().isModelPartShown(part);
    }

    @Override
    public double xCloakO() {
        return getPlayerOwner().xCloakO;
    }

    @Override
    public double yCloakO() {
        return getPlayerOwner().yCloakO;
    }

    @Override
    public double zCloakO() {
        return getPlayerOwner().zCloakO;
    }

    @Override
    public double xCloak() {
        return getPlayerOwner().xCloak;
    }

    @Override
    public double yCloak() {
        return getPlayerOwner().yCloak;
    }

    @Override
    public double zCloak() {
        return getPlayerOwner().zCloak;
    }

    @Override
    public float oBob() {
        return getPlayerOwner().oBob;
    }

    @Override
    public float bob() {
        return getPlayerOwner().bob;
    }

    @Override
    public Scoreboard getScoreboard() {
        return getPlayerOwner().getScoreboard();
    }

    @Override
    public Vec3 getDeltaMovementLerped(float partialTick) {
        if (getPlayerOwner() instanceof AbstractClientPlayer player)
            return player.getDeltaMovementLerped(partialTick);
        throw new IllegalStateException("KamikotizedMinion#getDeltaMovementLerped() called on non-client player!");
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }
}
