package dev.thomasglasser.mineraculous.world.effect;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class CataclysmMobEffect extends MobEffect {
    protected CataclysmMobEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);

        addAttributeModifier(Attributes.ATTACK_DAMAGE, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_KNOCKBACK, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ENTITY_INTERACTION_RANGE, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.JUMP_STRENGTH, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.MINING_EFFICIENCY, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_EFFICIENCY, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.OXYGEN_BONUS, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.SUBMERGED_MINING_SPEED, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.WATER_MOVEMENT_EFFICIENCY, Mineraculous.modLoc("effect.cataclysm"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        addAttributeModifier(Attributes.FALL_DAMAGE_MULTIPLIER, Mineraculous.modLoc("effect.cataclysm"), 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        entity.hurt(entity.damageSources().source(MineraculousDamageTypes.CATACLYSM), 1.0F);
        if (entity instanceof Player player) {
            player.causeFoodExhaustion(0.005F * (amplifier + 1.0F));
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int i = 40 >> amplifier;
        return i == 0 || duration % i == 0;
    }
}
