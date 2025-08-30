package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.entity.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements Leashable {
    @Unique
    private final Player mineraculous$instance = (Player) (Object) this;

    @Unique
    @Nullable
    private Leashable.LeashData mineraculous$leashData;

    private PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "getName", at = @At("RETURN"))
    private Component formatMiraculousName(Component original) {
        return MineraculousEntityUtils.formatDisplayName(mineraculous$instance, original);
    }

    @ModifyReturnValue(method = "decorateDisplayNameComponent", at = @At(value = "RETURN"))
    private MutableComponent formatMiraculousDisplayName(MutableComponent original) {
        return MineraculousEntityUtils.formatDisplayName(mineraculous$instance, original).copy();
    }

    @Override
    public void swing(InteractionHand hand, boolean updateSelf) {
        AbilityEffectData abilityEffectData = getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
        if (level() instanceof ServerLevel level && abilityEffectData.spectatingId().isPresent() && abilityEffectData.allowRemoteDamage()) {
            Entity target = level.getEntity(abilityEffectData.spectatingId().get());
            if (target instanceof LivingEntity livingEntity && livingEntity.getHealth() > 4) {
                target.hurt(level.damageSources().playerAttack(mineraculous$instance), 20);
                target.hurtMarked = true;
            }
        } else {
            super.swing(hand, updateSelf);
        }
    }

    @Nullable
    @Override
    public LeashData getLeashData() {
        return this.mineraculous$leashData;
    }

    @Override
    public void setLeashData(@Nullable LeashData leashData) {
        this.mineraculous$leashData = leashData;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public void elasticRangeLeashBehaviour(Entity leashHolder, float distance) {
        Leashable.super.elasticRangeLeashBehaviour(leashHolder, distance);
        hurtMarked = true;
    }
}
