package dev.thomasglasser.mineraculous.client.renderer.entity.state;

public interface MineraculousLivingEntityRenderState {
    boolean mineraculous$isCataclysmed();

    void mineraculous$setCataclysmed(boolean cataclysmed);

    float mineraculous$maxHealth();

    void mineraculous$setMaxHealth(float maxHealth);

    float mineraculous$health();

    void mineraculous$setHealth(float health);

    boolean mineraculous$isTransformed();

    void mineraculous$setTransformed(boolean transformed);
}
