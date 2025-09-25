package dev.thomasglasser.mineraculous.api.world.effect;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.damagesource.MineraculousDamageTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CataclysmMobEffect extends MobEffect {
    private static final ResourceLocation MODIFIER_ID = MineraculousConstants.modLoc("effect.cataclysm");

    public CataclysmMobEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);

        addAttributeModifier(Attributes.ATTACK_DAMAGE, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.BLOCK_BREAK_SPEED, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_KNOCKBACK, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.BLOCK_INTERACTION_RANGE, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.ENTITY_INTERACTION_RANGE, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.JUMP_STRENGTH, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.MINING_EFFICIENCY, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_EFFICIENCY, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.OXYGEN_BONUS, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.SUBMERGED_MINING_SPEED, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.WATER_MOVEMENT_EFFICIENCY, MODIFIER_ID, -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        addAttributeModifier(Attributes.FALL_DAMAGE_MULTIPLIER, MODIFIER_ID, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.hurt(livingEntity.damageSources().source(MineraculousDamageTypes.CATACLYSM), Math.max(amplifier * 1f, 1));
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int i = 40 >> amplifier;
        return i == 0 || duration % i == 0;
    }
}
