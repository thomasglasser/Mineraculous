package dev.thomasglasser.mineraculous.impl.world.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousTiers;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ModeTool;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ToolMode;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ToolModes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownButterflyCane;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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

public class ButterflyCaneItem extends SwordItem implements GeoItem, ProjectileItem, ModeTool {
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String ANIMATION_OPEN = "open";
    public static final String ANIMATION_CLOSE = "close";
    public static final String ANIMATION_SHEATHE = "sheathe";
    public static final String ANIMATION_SHEATHE_AND_OPEN = "sheathe_and_open";
    public static final String ANIMATION_UNSHEATHE = "unsheathe";
    public static final String ANIMATION_CLOSE_AND_UNSHEATHE = "close_and_unsheathe";

    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("misc.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("misc.close");
    private static final RawAnimation SHEATHE = RawAnimation.begin().thenPlay("attack.sheathe");
    private static final RawAnimation SHEATHE_AND_OPEN = RawAnimation.begin().then("attack.sheathe", Animation.LoopType.PLAY_ONCE).thenPlay("misc.open");
    private static final RawAnimation UNSHEATHE = RawAnimation.begin().thenPlay("attack.unsheathe");
    private static final RawAnimation CLOSE_AND_UNSHEATHE = RawAnimation.begin().then("misc.close", Animation.LoopType.PLAY_ONCE).thenPlay("attack.unsheathe");

    private static final ImmutableSet<ToolMode> TOOL_MODES = ImmutableSet.of(ToolModes.BLADE, ToolModes.BLOCK, ToolModes.KAMIKO_STORE, ToolModes.PHONE, ToolModes.SPYGLASS, ToolModes.THROW);

    private static final ItemAttributeModifiers BLADE = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 15, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, 0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ENTITY_INTERACTION_RANGE_ID, 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    private static final ItemAttributeModifiers COVERED = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ButterflyCaneItem(Properties properties) {
        super(MineraculousTiers.MIRACULOUS, properties
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
                else {
                    ToolMode mode = ModeTool.getToolMode(stack);
                    if (mode == ToolModes.BLADE)
                        return state.setAndContinue(DefaultAnimations.IDLE);
                    else if (mode == ToolModes.PHONE || mode == ToolModes.SPYGLASS)
                        return state.setAndContinue(OPEN);
                    else if (mode == ToolModes.KAMIKO_STORE) {
                        boolean hasNoStoredEntities = true;
                        Level level = ClientUtils.getLevel();
                        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
                        if (ownerId != null && level != null) {
                            Entity owner = level.getEntities().get(ownerId);
                            hasNoStoredEntities = owner != null && !owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).hasStoredEntities(MiraculousTags.CAN_USE_BUTTERFLY_CANE);
                        }
                        if (hasNoStoredEntities)
                            return state.setAndContinue(OPEN);
                    }
                }
            }
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_OPEN, OPEN)
                .triggerableAnim(ANIMATION_CLOSE, CLOSE)
                .triggerableAnim(ANIMATION_SHEATHE, SHEATHE)
                .triggerableAnim(ANIMATION_SHEATHE_AND_OPEN, SHEATHE_AND_OPEN)
                .triggerableAnim(ANIMATION_UNSHEATHE, UNSHEATHE)
                .triggerableAnim(ANIMATION_CLOSE_AND_UNSHEATHE, CLOSE_AND_UNSHEATHE));
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new ThrownButterflyCane(position.x(), position.y(), position.z(), level, itemStack);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        MineraculousItemUtils.checkHelicopterSlowFall(stack, entity);

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
        if (ModeTool.getToolMode(stack) == ToolModes.KAMIKO_STORE && interactionTarget instanceof Kamiko kamiko && ownerId != null) {
            Entity caneOwner = player.level().getEntities().get(ownerId);
            if (caneOwner != null) {
                MiraculousesData miraculousesData = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                Holder<Miraculous> storingKey = miraculousesData.getFirstTransformedIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE);
                MiraculousData storingData = miraculousesData.get(storingKey);
                if (storingData != null && storingData.storedEntities().isEmpty()) {
                    if (player.level() instanceof ServerLevel serverLevel) {
                        long animId = GeoItem.getOrAssignId(stack, serverLevel);
                        triggerAnim(player, animId, CONTROLLER_USE, ANIMATION_CLOSE);
                        CompoundTag kamikoTag = new CompoundTag();
                        kamiko.save(kamikoTag);
                        storingData.withStoredEntities(ImmutableList.of(kamikoTag)).save(storingKey, caneOwner);
                    }
                    kamiko.discard();
                    player.setItemInHand(usedHand, stack);
                    return InteractionResult.sidedSuccess(player.level().isClientSide);
                }
            }
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode != null) {
            UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
            if (mode == ToolModes.BLOCK || mode == ToolModes.THROW || mode == ToolModes.BLADE) {
                player.startUsingItem(hand);
            } else if (mode == ToolModes.SPYGLASS) {
                level.playSound(null, player, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.startUsingItem(hand);
            } else if (ownerId != null) {
                Entity caneOwner = player.level().getEntities().get(ownerId);
                if (caneOwner != null) {
                    MiraculousesData miraculousesData = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                    Holder<Miraculous> storingKey = miraculousesData.getFirstTransformedIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE);
                    MiraculousData storingData = miraculousesData.get(storingKey);
                    if (mode == ToolModes.KAMIKO_STORE && level instanceof ServerLevel serverLevel && storingData != null) {
                        ImmutableList<CompoundTag> stored = storingData.storedEntities();
                        if (!stored.isEmpty()) {
                            Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().create(serverLevel);
                            if (kamiko != null) {
                                kamiko.load(stored.getFirst());
                                kamiko.setPos(player.position().add(0, 1, 0));
                                serverLevel.addFreshEntity(kamiko);
                                triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), CONTROLLER_USE, ANIMATION_OPEN);
                                storingData.withStoredEntities(stored.subList(1, stored.size())).save(storingKey, caneOwner);
                            }
                        }
                    }
                } else
                    return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (stack.has(MineraculousDataComponents.BLOCKING) && remainingUseDuration % 10 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.GENERIC_SPIN.get());
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.THROW || mode == ToolModes.BLADE) {
            int i = this.getUseDuration(stack, livingEntity) - timeLeft;
            if (i >= 10) {
                if (!level.isClientSide) {
                    ThrownButterflyCane thrown = new ThrownButterflyCane(level, livingEntity, stack);
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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.BLOCK)
            return UseAnim.BLOCK;
        else if (mode == ToolModes.BLADE || mode == ToolModes.THROW)
            return UseAnim.SPEAR;
        else if (mode == ToolModes.SPYGLASS)
            return UseAnim.SPYGLASS;
        return UseAnim.NONE;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.BLOCK)
            return itemAbility == ItemAbilities.SHIELD_BLOCK;
        else if (mode == ToolModes.THROW)
            return itemAbility == ItemAbilities.TRIDENT_THROW;
        else if (mode == ToolModes.SPYGLASS)
            return itemAbility == ItemAbilities.SPYGLASS_SCOPE;
        return false;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (ModeTool.getToolMode(stack) == ToolModes.BLADE)
            return BLADE;
        return COVERED;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    @Override
    public ImmutableSet<ToolMode> getToolModes(ItemStack stack, InteractionHand hand, Player holder) {
        return TOOL_MODES;
    }

    @Override
    public boolean isEnabled(ToolMode mode, ItemStack stack, InteractionHand hand, Player holder) {
        if (mode == ToolModes.KAMIKO_STORE && !stack.has(MineraculousDataComponents.OWNER))
            return false;
        return mode != ToolModes.PHONE || MineraculousConstants.Dependencies.TOMMYTECH.isLoaded();
    }

    @Override
    public void onModeChanged(ItemStack stack, InteractionHand hand, Player holder, @Nullable ToolMode oldMode, @Nullable ToolMode newMode) {
        if (holder != null && holder.level() instanceof ServerLevel level) {
            String anim = null;
            boolean hasNoStoredEntities = false;
            UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
            if (ownerId != null) {
                Entity owner = level.getEntities().get(ownerId);
                hasNoStoredEntities = owner != null && !owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).hasStoredEntities(MiraculousTags.CAN_USE_BUTTERFLY_CANE);
            }
            if (newMode == ToolModes.BLADE) {
                if ((oldMode == ToolModes.KAMIKO_STORE && hasNoStoredEntities) || oldMode == ToolModes.SPYGLASS || oldMode == ToolModes.PHONE)
                    anim = ANIMATION_CLOSE_AND_UNSHEATHE;
                else
                    anim = ANIMATION_UNSHEATHE;
            } else if ((newMode == ToolModes.KAMIKO_STORE && hasNoStoredEntities) || newMode == ToolModes.SPYGLASS || newMode == ToolModes.PHONE) {
                if (oldMode == ToolModes.BLADE)
                    anim = ANIMATION_SHEATHE_AND_OPEN;
                else
                    anim = ANIMATION_OPEN;
            } else if ((oldMode == ToolModes.KAMIKO_STORE && hasNoStoredEntities) || oldMode == ToolModes.SPYGLASS || oldMode == ToolModes.PHONE)
                anim = ANIMATION_CLOSE;
            else if (oldMode == ToolModes.BLADE)
                anim = ANIMATION_SHEATHE;
            if (anim != null) {
                triggerAnim(holder, GeoItem.getOrAssignId(stack, level), CONTROLLER_USE, anim);
            }
        }
    }
}
