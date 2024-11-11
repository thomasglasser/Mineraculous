package dev.thomasglasser.mineraculous.mixin.minecraft.client.renderer.entity.state;

import dev.thomasglasser.mineraculous.client.renderer.entity.state.MineraculousLivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements MineraculousLivingEntityRenderState {
    @Unique
    private boolean mineraculous$cataclysmed = false;
    @Unique
    private float mineraculous$maxHealth = 0;
    @Unique
    private float mineraculous$health = 0;
    @Unique
    private boolean mineraculous$transformed = false;
    @Unique
    private boolean mineraculous$showKamikoMask = false;

    @Override
    public boolean mineraculous$isCataclysmed() {
        return this.mineraculous$cataclysmed;
    }

    @Override
    public void mineraculous$setCataclysmed(boolean cataclysmed) {
        this.mineraculous$cataclysmed = cataclysmed;
    }

    @Override
    public float mineraculous$maxHealth() {
        return this.mineraculous$maxHealth;
    }

    @Override
    public void mineraculous$setMaxHealth(float maxHealth) {
        this.mineraculous$maxHealth = maxHealth;
    }

    @Override
    public float mineraculous$health() {
        return this.mineraculous$health;
    }

    @Override
    public void mineraculous$setHealth(float health) {
        this.mineraculous$health = health;
    }

    @Override
    public boolean mineraculous$isTransformed() {
        return this.mineraculous$transformed;
    }

    @Override
    public void mineraculous$setTransformed(boolean transformed) {
        this.mineraculous$transformed = transformed;
    }

    @Override
    public boolean mineraculous$showKamikoMask() {
        return this.mineraculous$showKamikoMask;
    }

    @Override
    public void mineraculous$setShowKamikoMask(boolean showKamikoMask) {
        this.mineraculous$showKamikoMask = showKamikoMask;
    }
}
