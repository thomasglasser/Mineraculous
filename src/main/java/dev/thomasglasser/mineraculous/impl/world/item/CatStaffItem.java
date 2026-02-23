package dev.thomasglasser.mineraculous.impl.world.item;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.ActiveItem;
import dev.thomasglasser.mineraculous.api.world.item.LeftClickListener;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousTiers;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ModeTool;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ToolMode;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ToolModes;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffPerchHandler;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffTravelHandler;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class CatStaffItem extends SwordItem implements GeoItem, ProjectileItem, ICurioItem, LeftClickListener, ActiveItem, ModeTool {
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String CONTROLLER_EXTEND = "extend_controller";
    public static final String ANIMATION_EXTEND = "extend";
    public static final String ANIMATION_RETRACT = "retract";
    public static final String ANIMATION_OPEN = "open";
    public static final String ANIMATION_RETRACT_AND_OPEN = "retract_and_open";
    public static final String ANIMATION_CLOSE = "close";
    public static final String ANIMATION_CLOSE_AND_EXTEND = "close_and_extend";

    private static final RawAnimation EXTEND = RawAnimation.begin().thenPlay("misc.extend");
    private static final RawAnimation RETRACT = RawAnimation.begin().thenPlay("misc.retract");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("misc.open");
    private static final RawAnimation RETRACT_AND_OPEN = RawAnimation.begin().then("misc.retract", Animation.LoopType.PLAY_ONCE).thenPlay("misc.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("misc.close");
    private static final RawAnimation CLOSE_AND_EXTEND = RawAnimation.begin().then("misc.close", Animation.LoopType.PLAY_ONCE).thenPlay("misc.extend");

    private static final ImmutableSet<ToolMode> TOOL_MODES = ImmutableSet.of(ToolModes.BLOCK, ToolModes.PERCH, ToolModes.PHONE, ToolModes.SPYGLASS, ToolModes.THROW, ToolModes.TRAVEL);

    private static final ItemAttributeModifiers EXTENDED_ATTRIBUTE_MODIFIERS = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 15, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -1.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ENTITY_INTERACTION_RANGE_ID, 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public CatStaffItem(Properties pProperties) {
        super(MineraculousTiers.MIRACULOUS, pProperties
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_USE, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack != null) {
                if (stack.has(MineraculousDataComponents.BLOCKING))
                    return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
                Integer carrierId = stack.get(MineraculousDataComponents.CARRIER);
                if (carrierId != null) {
                    Entity entity = ClientUtils.getEntityById(carrierId);
                    if (entity instanceof Player player && player.getUseItem() == stack && !player.getCooldowns().isOnCooldown(stack.getItem()) && !player.onGround() && ModeTool.getToolMode(stack) == ToolModes.TRAVEL && !player.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF).traveling()) {
                        return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
                    }
                }
            }
            return PlayState.STOP;
        }));
        controllers.add(new AnimationController<>(this, CONTROLLER_EXTEND, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack != null) {
                if (!Active.isActive(stack))
                    return state.setAndContinue(DefaultAnimations.IDLE);
                ToolMode mode = ModeTool.getToolMode(stack);
                if (mode == ToolModes.PHONE || mode == ToolModes.SPYGLASS)
                    return state.setAndContinue(OPEN);
            }
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_EXTEND, EXTEND)
                .triggerableAnim(ANIMATION_RETRACT, RETRACT)
                .triggerableAnim(ANIMATION_OPEN, OPEN)
                .triggerableAnim(ANIMATION_RETRACT_AND_OPEN, RETRACT_AND_OPEN)
                .triggerableAnim(ANIMATION_CLOSE, CLOSE)
                .triggerableAnim(ANIMATION_CLOSE_AND_EXTEND, CLOSE_AND_EXTEND));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof LivingEntity livingEntity) {
            if (Active.isActive(stack)) {
                boolean inHand = livingEntity.getMainHandItem() == stack || livingEntity.getOffhandItem() == stack;
                ToolMode mode = ModeTool.getToolMode(stack);
                if (mode != null) {
                    PerchingCatStaffData perchingData = livingEntity.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
                    TravelingCatStaffData travelingData = livingEntity.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF);
                    if (inHand) {
                        if (mode == ToolModes.PERCH)
                            CatStaffPerchHandler.tick(level, livingEntity);
                        else if (mode == ToolModes.TRAVEL)
                            CatStaffTravelHandler.tick(stack, level, livingEntity);
                        else {
                            if (!level.isClientSide) {
                                if (!perchingData.equals(PerchingCatStaffData.DEFAULT))
                                    PerchingCatStaffData.remove(livingEntity);
                                if (!travelingData.equals(TravelingCatStaffData.DEFAULT))
                                    TravelingCatStaffData.remove(livingEntity);
                            }
                        }
                    } else {
                        if (!level.isClientSide) {
                            if (perchingData != PerchingCatStaffData.DEFAULT)
                                PerchingCatStaffData.remove(livingEntity);
                            if (travelingData != TravelingCatStaffData.DEFAULT)
                                TravelingCatStaffData.remove(livingEntity);
                        }
                    }
                }
            } else {
                if (livingEntity.hasData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF)) {
                    PerchingCatStaffData.remove(livingEntity);
                }
                if (livingEntity.hasData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF)) {
                    TravelingCatStaffData.remove(livingEntity);
                }
            }
        }
        MineraculousItemUtils.checkHelicopterSlowFall(stack, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!Active.isActive(stack))
            return InteractionResultHolder.fail(stack);
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode != null) {
            if (mode == ToolModes.BLOCK || mode == ToolModes.THROW || mode == ToolModes.TRAVEL)
                player.startUsingItem(hand);
            else if (mode == ToolModes.PERCH) {
                PerchingCatStaffData perchingCatStaffData = player.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
                CatStaffPerchHandler.itemUsed(level, player, perchingCatStaffData);
                player.awardStat(Stats.ITEM_USED.get(this));
            } else if (mode == ToolModes.SPYGLASS) {
                level.playSound(null, player, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.startUsingItem(hand);
            }
            if (mode == ToolModes.TRAVEL)
                CatStaffTravelHandler.init(level, player);
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (livingEntity instanceof Player player) {
            boolean travelEligible = !player.getCooldowns().isOnCooldown(stack.getItem()) && !player.onGround() && ModeTool.getToolMode(stack) == ToolModes.TRAVEL && !player.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF).traveling() && stack.getUseDuration(livingEntity) - remainingUseDuration > 1;
            if ((stack.has(MineraculousDataComponents.BLOCKING) || travelEligible) && remainingUseDuration % 10 == 0) {
                player.playSound(MineraculousSoundEvents.GENERIC_SPIN.get());
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.THROW) {
            int i = this.getUseDuration(stack, livingEntity) - timeLeft;
            if (i >= 10) {
                if (!level.isClientSide) {
                    ThrownCatStaff thrown = new ThrownCatStaff(level, livingEntity, stack);
                    thrown.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot(), 0.0F, 2.5F, 1.0F);
                    stack.hurtAndBreak(1, livingEntity, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
                    if (livingEntity.hasInfiniteMaterials()) {
                        thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }

                    level.addFreshEntity(thrown);
                    level.playSound(null, thrown, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (!livingEntity.hasInfiniteMaterials() && livingEntity instanceof Player player) {
                        player.getInventory().removeItem(stack);
                    }
                }

                if (livingEntity instanceof Player player) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.BLOCK)
            return UseAnim.BLOCK;
        if (mode == ToolModes.SPYGLASS)
            return UseAnim.SPYGLASS;
        if (mode == ToolModes.THROW)
            return UseAnim.SPEAR;
        return UseAnim.NONE;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return onLeftClick(stack, player);
    }

    @Override
    public boolean onLeftClick(ItemStack stack, LivingEntity livingEntity) {
        if (Active.isActive(stack)) {
            ToolMode mode = ModeTool.getToolMode(stack);
            if (mode == ToolModes.PERCH) {
                PerchingCatStaffData perchingCatStaffData = livingEntity.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
                CatStaffPerchHandler.itemLeftClicked(livingEntity.level(), livingEntity, perchingCatStaffData);
                if (livingEntity instanceof Player player) {
                    player.awardStat(Stats.ITEM_USED.get(this));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.BLOCK)
            return itemAbility == ItemAbilities.SHIELD_BLOCK;
        if (mode == ToolModes.SPYGLASS)
            return itemAbility == ItemAbilities.SPYGLASS_SCOPE;
        if (mode == ToolModes.THROW)
            return itemAbility == ItemAbilities.TRIDENT_THROW;
        return false;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (Active.isActive(stack) && mode != ToolModes.PHONE && mode != ToolModes.SPYGLASS)
            return EXTENDED_ATTRIBUTE_MODIFIERS;
        return super.getDefaultAttributeModifiers(stack);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new ThrownCatStaff(level, pos.x(), pos.y(), pos.z(), stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return canEquip(stack);
    }

    public boolean canEquip(ItemStack stack) {
        return !Active.isActive(stack);
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        if (canEquip(stack)) {
            return ICurioItem.super.getSlotsTooltip(tooltips, context, stack);
        }
        return List.of();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    @Override
    public void onToggle(ItemStack stack, @Nullable Entity holder, Active active) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (holder != null) {
            String anim;
            SoundEvent sound;
            if (active.active()) {
                if (mode == ToolModes.PHONE || mode == ToolModes.SPYGLASS)
                    anim = ANIMATION_OPEN;
                else
                    anim = ANIMATION_EXTEND;
                sound = MineraculousSoundEvents.CAT_STAFF_EXTEND.get();
            } else {
                if (mode == ToolModes.PHONE || mode == ToolModes.SPYGLASS)
                    anim = ANIMATION_CLOSE;
                else
                    anim = ANIMATION_RETRACT;
                sound = MineraculousSoundEvents.CAT_STAFF_RETRACT.get();
            }
            triggerAnim(holder, GeoItem.getOrAssignId(stack, (ServerLevel) holder.level()), CONTROLLER_EXTEND, anim);
            holder.level().playSound(null, holder.blockPosition(), sound, holder.getSoundSource(), 1.0F, 1.0F);
        }
    }

    @Override
    public ImmutableSet<ToolMode> getToolModes(ItemStack stack, InteractionHand hand, Player holder) {
        return TOOL_MODES;
    }

    @Override
    public boolean isEnabled(ToolMode mode, ItemStack stack, InteractionHand hand, Player holder) {
        return mode != ToolModes.PHONE || MineraculousConstants.Dependencies.TOMMYTECH.isLoaded();
    }

    @Override
    public boolean canChangeToolMode(ItemStack stack, InteractionHand hand, Player holder) {
        return Active.isActive(stack);
    }

    @Override
    public void onModeChanged(ItemStack stack, InteractionHand hand, Player holder, @Nullable ToolMode oldMode, @Nullable ToolMode newMode) {
        if (holder != null && holder.level() instanceof ServerLevel level) {
            String anim = null;
            SoundEvent sound = null;
            if (newMode == ToolModes.PHONE || newMode == ToolModes.SPYGLASS) {
                if (oldMode == ToolModes.PHONE || oldMode == ToolModes.SPYGLASS)
                    anim = ANIMATION_OPEN;
                else {
                    anim = ANIMATION_RETRACT_AND_OPEN;
                    sound = MineraculousSoundEvents.CAT_STAFF_RETRACT.get();
                }
            } else if (oldMode == ToolModes.PHONE || oldMode == ToolModes.SPYGLASS) {
                anim = ANIMATION_CLOSE_AND_EXTEND;
                sound = MineraculousSoundEvents.CAT_STAFF_EXTEND.get();
            }
            if (anim != null)
                triggerAnim(holder, GeoItem.getOrAssignId(stack, level), CONTROLLER_EXTEND, anim);
            if (sound != null)
                level.playSound(null, holder.blockPosition(), sound, holder.getSoundSource(), 1.0F, 1.0F);
        }
    }
}
