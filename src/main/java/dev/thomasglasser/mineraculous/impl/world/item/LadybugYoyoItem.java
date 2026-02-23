package dev.thomasglasser.mineraculous.impl.world.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.ActiveItem;
import dev.thomasglasser.mineraculous.api.world.item.LeftClickListener;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ModeTool;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ToolMode;
import dev.thomasglasser.mineraculous.api.world.item.toolmode.ToolModes;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LeashingLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
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

public class LadybugYoyoItem extends Item implements GeoItem, ICurioItem, ActiveItem, LeftClickListener, ModeTool {
    public static final String CONTROLLER_USE = "use_controller";
    public static final String ANIMATION_OPEN_OUT = "open_out";
    public static final String ANIMATION_OPEN_DOWN = "open_down";
    public static final String ANIMATION_CLOSE_IN = "close_in";
    public static final String ANIMATION_CLOSE_IN_AND_OPEN_DOWN = "close_in_and_open_down";
    public static final String ANIMATION_CLOSE_UP = "close_up";
    public static final String ANIMATION_CLOSE_UP_AND_OPEN_OUT = "close_up_and_open_out";

    private static final RawAnimation OPEN_OUT = RawAnimation.begin().thenPlay("misc.open_out");
    private static final RawAnimation OPEN_DOWN = RawAnimation.begin().thenPlay("misc.open_down");
    private static final RawAnimation CLOSE_IN = RawAnimation.begin().thenPlay("misc.close_in");
    private static final RawAnimation CLOSE_IN_AND_OPEN_DOWN = RawAnimation.begin().then("misc.close_in", Animation.LoopType.PLAY_ONCE).thenPlay("misc.open_down");
    private static final RawAnimation CLOSE_UP = RawAnimation.begin().thenPlay("misc.close_up");
    private static final RawAnimation CLOSE_UP_AND_OPEN_OUT = RawAnimation.begin().then("misc.close_up", Animation.LoopType.PLAY_ONCE).thenPlay("misc.open_out");

    private static final ImmutableSet<ToolMode> TOOL_MODES = ImmutableSet.of(ToolModes.BLOCK, ToolModes.LASSO, ToolModes.PHONE, ToolModes.PURIFY, ToolModes.SPYGLASS, ToolModes.TRAVEL);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LadybugYoyoItem(Properties properties) {
        super(properties
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public static void removeHeldLeash(Entity holder) {
        holder.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).ifPresent(data -> {
            Entity leashed = holder.level().getEntity(data.leashedId());
            if (leashed != null) {
                removeLeash(leashed, holder);
            }
        });
    }

    public static void removeLeashFrom(Entity leashed) {
        if (leashed instanceof Leashable leashable) {
            Entity holder = leashable.getLeashHolder();
            if (holder != null) {
                removeLeash(leashed, holder);
            }
        }
    }

    public static void removeLeash(Entity leashed, Entity holder) {
        if (leashed instanceof Leashable leashable) {
            leashable.dropLeash(true, false);
        }
        leashed.setData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE, false);
        LeashingLadybugYoyoData.remove(holder);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_USE, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack != null && Active.isActive(stack)) {
                ToolMode mode = ModeTool.getToolMode(stack);
                if (mode == ToolModes.PURIFY)
                    return state.setAndContinue(DefaultAnimations.IDLE);
                else if (mode == ToolModes.PHONE || mode == ToolModes.SPYGLASS)
                    return state.setAndContinue(OPEN_DOWN);
            }
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_OPEN_OUT, OPEN_OUT)
                .triggerableAnim(ANIMATION_OPEN_DOWN, OPEN_DOWN)
                .triggerableAnim(ANIMATION_CLOSE_IN, CLOSE_IN)
                .triggerableAnim(ANIMATION_CLOSE_IN_AND_OPEN_DOWN, CLOSE_IN_AND_OPEN_DOWN)
                .triggerableAnim(ANIMATION_CLOSE_UP, CLOSE_UP)
                .triggerableAnim(ANIMATION_CLOSE_UP_AND_OPEN_OUT, CLOSE_UP_AND_OPEN_OUT));
        controllers.add(new AnimationController<>(this, "blocking_controller", state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack != null && stack.has(MineraculousDataComponents.BLOCKING))
                return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity && !livingEntity.isUsingItem()) {
            if (!level.isClientSide()) {
                ThrownLadybugYoyoData data = livingEntity.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                if (data.safeFallTicks() > 0) {
                    livingEntity.resetFallDistance();
                    data.decrementSafeFallTicks().save(livingEntity);
                }
            }
        }

        MineraculousItemUtils.checkHelicopterSlowFall(stack, entity);

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!Active.isActive(stack))
            return InteractionResultHolder.fail(stack);
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode != null) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                if (level instanceof ServerLevel serverLevel) {
                    ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                    if (data.id().isPresent()) {
                        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(serverLevel);
                        if (thrownYoyo != null) {
                            if (thrownYoyo.isRecalling() && mode == thrownYoyo.getToolMode()) {
                                thrownYoyo.discard();
                                throwYoyo(stack, player, ModeTool.getToolMode(stack), usedHand);
                                player.getCooldowns().addCooldown(this, 5);
                            } else {
                                recallYoyo(player);
                                Vec3 inertia = player.getDeltaMovement();
                                if (inertia.length() > 0.7) inertia = inertia.add(0, 0.5, 0);
                                player.setDeltaMovement(inertia.scale(2.5));
                            }
                        } else {
                            data.clearId().save(player);
                        }
                    } else if (mode == ToolModes.BLOCK) {
                        player.startUsingItem(usedHand);
                    } else if (mode == ToolModes.LASSO && player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
                        removeHeldLeash(player);
                    } else if (mode == ToolModes.PURIFY) {
                        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
                        Entity owner = ownerId != null ? serverLevel.getEntity(ownerId) : null;
                        if (owner != null) {
                            MiraculousesData miraculousesData = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                            Holder<Miraculous> storingKey = miraculousesData.getFirstTransformedIn(MiraculousTags.CAN_USE_LADYBUG_YOYO);
                            MiraculousData storingData = miraculousesData.get(storingKey);
                            if (storingData != null) {
                                ImmutableList<CompoundTag> stored = storingData.storedEntities();
                                if (!stored.isEmpty()) {
                                    ImmutableList.Builder<Entity> released = new ImmutableList.Builder<>();
                                    for (CompoundTag tag : stored) {
                                        Entity entity = EntityType.loadEntityRecursive(tag, level, loaded -> {
                                            loaded.setPos(player.getX(), player.getY() + 0.5, player.getZ());
                                            return loaded;
                                        });
                                        if (entity != null) {
                                            serverLevel.addFreshEntity(entity);
                                            EntityReversionData.get(serverLevel).revertConversionOrCopy(entity.getUUID(), serverLevel, reverted -> {
                                                reverted.moveTo(player.position().add(0, 1, 0));
                                                reverted.addDeltaMovement(new Vec3(0, 1, 0));
                                                reverted.hurtMarked = true;
                                                released.add(reverted);
                                            });
                                        }
                                    }
                                    MineraculousCriteriaTriggers.RELEASED_PURIFIED_ENTITIES.get().trigger((ServerPlayer) player, released.build());
                                    storingData.withStoredEntities(ImmutableList.of()).save(storingKey, owner);
                                    player.getCooldowns().addCooldown(this, 10);
                                } else {
                                    player.startUsingItem(usedHand);
                                }
                            }
                        }
                    } else if (mode == ToolModes.SPYGLASS) {
                        level.playSound(null, player, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                        player.startUsingItem(usedHand);
                    } else if (usedHand == InteractionHand.MAIN_HAND || mode != ToolModes.LASSO) {
                        throwYoyo(stack, player, mode, usedHand);
                        player.getCooldowns().addCooldown(this, 5);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (stack.has(MineraculousDataComponents.BLOCKING) && remainingUseDuration % 7 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.LADYBUG_YOYO_SPIN.get());
        }
    }

    @Override
    public boolean onLeftClick(ItemStack stack, LivingEntity entity) {
        if (Active.isActive(stack) && entity instanceof Player player && !player.getCooldowns().isOnCooldown(this)) {
            if (player.level() instanceof ServerLevel serverLevel) {
                ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                if (data.id().isPresent()) {
                    ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(serverLevel);
                    if (thrownYoyo != null) {
                        if (thrownYoyo.getToolMode() == ToolModes.TRAVEL) {
                            if (thrownYoyo.inGround()) {
                                Vec3 fromPlayerToYoyo = new Vec3(thrownYoyo.getX() - player.getX(), thrownYoyo.getY() - player.getY() + 1, thrownYoyo.getZ() - player.getZ());
                                player.setDeltaMovement(fromPlayerToYoyo.scale(0.2).add(player.getDeltaMovement()));
                                player.hurtMarked = true;
                                data.startSafeFall().save(player);
                            }
                        }
                        recallYoyo(player);
                    }
                } else if (player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
                    Entity leashed = serverLevel.getEntity(player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).get().leashedId());
                    if (leashed != null) {
                        Vec3 fromLeashedToHolder = new Vec3(player.getX() - leashed.getX(), player.getY() - leashed.getY(), player.getZ() - leashed.getZ());
                        leashed.setDeltaMovement(fromLeashedToHolder.scale(0.25).add(leashed.getDeltaMovement()));
                        leashed.hurtMarked = true;
                    }
                } else {
                    throwYoyo(stack, player, ModeTool.getToolMode(stack) == ToolModes.PURIFY ? ToolModes.PURIFY : null, InteractionHand.MAIN_HAND);
                    player.getCooldowns().addCooldown(this, 5);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return onLeftClick(stack, player);
    }

    public void recallYoyo(Player player) {
        ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
        if (data.id().isPresent()) {
            Level level = player.level();
            if (!level.isClientSide) {
                ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(level);
                if (thrownYoyo != null) {
                    thrownYoyo.recall();
                    data.startSafeFall().save(player);
                }
            }
            level.playSound(null, player, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }
    }

    public void throwYoyo(ItemStack stack, Player player, ToolMode mode, InteractionHand hand) {
        Level level = player.level();
        if (!level.isClientSide) {
            ThrownLadybugYoyo thrown = new ThrownLadybugYoyo(player, level, stack, mode);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            thrown.setInitialDirection(player.getDirection());
            thrown.setHand(hand);
            level.addFreshEntity(thrown);
            thrown.setNoGravity(true);
            level.playSound(null, thrown, SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.gameEvent(GameEvent.ITEM_INTERACT_START);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.BLOCK || mode == ToolModes.PURIFY)
            return UseAnim.BLOCK;
        if (mode == ToolModes.SPYGLASS)
            return UseAnim.SPYGLASS;
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.BLOCK || mode == ToolModes.SPYGLASS || mode == ToolModes.PURIFY) {
            return 72000;
        }
        return 0;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        ToolMode mode = ModeTool.getToolMode(stack);
        if (mode == ToolModes.BLOCK || mode == ToolModes.PURIFY)
            return itemAbility == ItemAbilities.SHIELD_BLOCK;
        if (mode == ToolModes.SPYGLASS)
            return itemAbility == ItemAbilities.SPYGLASS_SCOPE;
        return false;
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
        return ImmutableList.of();
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    @Override
    public void onToggle(ItemStack stack, @Nullable Entity holder, Active active) {
        if (holder != null) {
            ToolMode mode = ModeTool.getToolMode(stack);
            if (mode != null) {
                String anim = null;
                if (active.active()) {
                    if (mode == ToolModes.PHONE || mode == ToolModes.SPYGLASS)
                        anim = ANIMATION_OPEN_DOWN;
                    else if (mode == ToolModes.PURIFY)
                        anim = ANIMATION_OPEN_OUT;
                } else {
                    if (mode == ToolModes.PHONE || mode == ToolModes.SPYGLASS)
                        anim = ANIMATION_CLOSE_UP;
                    else if (mode == ToolModes.PURIFY)
                        anim = ANIMATION_CLOSE_IN;
                }
                if (anim != null) {
                    triggerAnim(holder, GeoItem.getOrAssignId(stack, (ServerLevel) holder.level()), CONTROLLER_USE, anim);
                }
            }
        }
    }

    @Override
    public ImmutableSet<ToolMode> getToolModes(ItemStack stack, InteractionHand hand, Player holder) {
        return TOOL_MODES;
    }

    @Override
    public boolean isEnabled(ToolMode mode, ItemStack stack, InteractionHand hand, Player holder) {
        if (mode == ToolModes.PHONE && !MineraculousConstants.Dependencies.TOMMYTECH.isLoaded())
            return false;
        return mode != ToolModes.PURIFY || stack.has(MineraculousDataComponents.OWNER);
    }

    @Override
    public boolean canChangeToolMode(ItemStack stack, InteractionHand hand, Player holder) {
        return Active.isActive(stack);
    }

    @Override
    public void onModeChanged(ItemStack stack, InteractionHand hand, Player holder, @Nullable ToolMode oldMode, @Nullable ToolMode newMode) {
        if (holder.level() instanceof ServerLevel level) {
            String anim = null;
            if (newMode == ToolModes.PHONE || newMode == ToolModes.SPYGLASS) {
                if (oldMode == ToolModes.PURIFY)
                    anim = ANIMATION_CLOSE_IN_AND_OPEN_DOWN;
                else
                    anim = ANIMATION_OPEN_DOWN;
            } else if (newMode == ToolModes.PURIFY) {
                if (oldMode == ToolModes.PHONE || oldMode == ToolModes.SPYGLASS)
                    anim = ANIMATION_CLOSE_UP_AND_OPEN_OUT;
                else
                    anim = ANIMATION_OPEN_OUT;
            } else if (oldMode == ToolModes.PHONE || oldMode == ToolModes.SPYGLASS)
                anim = ANIMATION_CLOSE_UP;
            else if (oldMode == ToolModes.PURIFY)
                anim = ANIMATION_CLOSE_IN;
            if (anim != null) {
                triggerAnim(holder, GeoItem.getOrAssignId(stack, level), CONTROLLER_USE, anim);
            }
        }
    }
}
