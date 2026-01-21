package dev.thomasglasser.mineraculous.impl.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.ActiveItem;
import dev.thomasglasser.mineraculous.api.world.item.LeftClickListener;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousTiers;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.network.ServerboundEquipToolPayload;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffPerchCommander;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffTravelCommander;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newTravelingCatStaffData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
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
import net.minecraft.world.phys.Vec2;
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

public class CatStaffItem extends SwordItem implements GeoItem, ProjectileItem, ICurioItem, RadialMenuProvider<CatStaffItem.Mode>, LeftClickListener, ActiveItem {
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String CONTROLLER_EXTEND = "extend_controller";
    public static final String ANIMATION_EXTEND = "extend";
    public static final String ANIMATION_RETRACT = "retract";
    public static final String ANIMATION_OPEN = "open";
    public static final String ANIMATION_RETRACT_AND_OPEN = "retract_and_open";
    public static final String ANIMATION_CLOSE = "close";
    public static final String ANIMATION_CLOSE_AND_EXTEND = "close_and_extend";

    public static final float DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS = 0.5f;
    public static final float STAFF_HEAD_ABOVE_USER_HEAD_OFFSET = 0.4f;
    public static final float USER_VERTICAL_MOVEMENT_SPEED = 0.5f;
    public static final double HORIZONTAL_MOVEMENT_THRESHOLD = 0.15d;
    public static final double HORIZONTAL_MOVEMENT_SCALE = 0.1d;
    public static final int STAFF_GROWTH_SPEED = 8;

    private static final RawAnimation EXTEND = RawAnimation.begin().thenPlay("misc.extend");
    private static final RawAnimation RETRACT = RawAnimation.begin().thenPlay("misc.retract");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("misc.open");
    private static final RawAnimation RETRACT_AND_OPEN = RawAnimation.begin().then("misc.retract", Animation.LoopType.PLAY_ONCE).thenPlay("misc.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("misc.close");
    private static final RawAnimation CLOSE_AND_EXTEND = RawAnimation.begin().then("misc.close", Animation.LoopType.PLAY_ONCE).thenPlay("misc.extend");

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
                    if (entity instanceof Player player && player.getUseItem() == stack && !player.getCooldowns().isOnCooldown(stack.getItem()) && !player.onGround() && stack.get(MineraculousDataComponents.CAT_STAFF_MODE) == Mode.TRAVEL && !player.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF).traveling()) {
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
                Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
                if (mode == Mode.PHONE || mode == Mode.SPYGLASS)
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
            Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
            boolean inHand = livingEntity.getMainHandItem() == stack || livingEntity.getOffhandItem() == stack;
            CatStaffPerchCommander.tick(level, entity, mode);
            CatStaffTravelCommander.tick(level, entity, mode);
            if (!inHand || !Active.isActive(stack)) {
                PerchingCatStaffData.remove(entity);
                newTravelingCatStaffData.remove(entity);
            }
        }
        MineraculousItemUtils.checkHelicopterSlowFall(stack, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!Active.isActive(stack))
            return InteractionResultHolder.fail(stack);
        if (stack.has(MineraculousDataComponents.CAT_STAFF_MODE)) {
            Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
            if (mode == Mode.BLOCK || mode == Mode.THROW || mode == Mode.TRAVEL)
                player.startUsingItem(hand);
            if (mode == Mode.PERCH) {
                CatStaffPerchCommander.itemUsed(level, player);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
            if (mode == Mode.TRAVEL) {
                CatStaffTravelCommander.itemUsed(level, player);
            }
            if (mode == Mode.SPYGLASS) {
                level.playSound(null, player, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.startUsingItem(hand);
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (livingEntity instanceof Player player) {
            boolean travelEligible = !player.getCooldowns().isOnCooldown(stack.getItem()) && !player.onGround() && stack.get(MineraculousDataComponents.CAT_STAFF_MODE) == Mode.TRAVEL && !player.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF).traveling() && stack.getUseDuration(livingEntity) - remainingUseDuration > 1;
            if ((stack.has(MineraculousDataComponents.BLOCKING) || travelEligible) && remainingUseDuration % 10 == 0) {
                player.playSound(MineraculousSoundEvents.GENERIC_SPIN.get());
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
        if (mode == Mode.THROW) {
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
        Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
        return switch (mode) {
            case BLOCK -> UseAnim.BLOCK;
            case SPYGLASS -> UseAnim.SPYGLASS;
            case THROW -> UseAnim.SPEAR;
            case null, default -> UseAnim.NONE;
        };
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return onLeftClick(stack, player);
    }

    @Override
    public boolean onLeftClick(ItemStack stack, LivingEntity livingEntity) {
        if (Active.isActive(stack)) {
            Level level = livingEntity.level();
            CatStaffPerchCommander.onLeftClick(level, livingEntity);
        }
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE.get());
        return switch (mode) {
            case BLOCK -> itemAbility == ItemAbilities.SHIELD_BLOCK;
            case SPYGLASS -> itemAbility == ItemAbilities.SPYGLASS_SCOPE;
            case THROW -> itemAbility == ItemAbilities.TRIDENT_THROW;
            case null, default -> false;
        };
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
        if (Active.isActive(stack) && mode != Mode.PHONE && mode != Mode.SPYGLASS)
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
    public boolean canOpenMenu(ItemStack stack, InteractionHand hand, Player holder) {
        return Active.isActive(stack);
    }

    @Override
    public int getColor(ItemStack stack, InteractionHand hand, Player holder) {
        Level level = holder.level();
        int color = level.holderOrThrow(Miraculouses.CAT).value().color().getValue();
        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
        if (ownerId != null) {
            Entity owner = level.getEntities().get(ownerId);
            if (owner != null) {
                Holder<Miraculous> colorKey = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).getFirstTransformedIn(MiraculousTags.CAN_USE_CAT_STAFF);
                if (colorKey != null)
                    color = colorKey.value().color().getValue();
            }
        }
        return color;
    }

    @Override
    public List<Mode> getOptions(ItemStack stack, InteractionHand hand, Player holder) {
        return Mode.valuesList();
    }

    @Override
    public Supplier<DataComponentType<Mode>> getComponentType(ItemStack stack, InteractionHand hand, Player holder) {
        return MineraculousDataComponents.CAT_STAFF_MODE;
    }

    @Override
    public boolean handleSecondaryKeyBehavior(ItemStack stack, InteractionHand hand, Player holder) {
        TommyLibServices.NETWORK.sendToServer(new ServerboundEquipToolPayload(hand));
        return true;
    }

    @Override
    public Mode setOption(ItemStack stack, InteractionHand hand, Player holder, int index) {
        Mode old = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
        Mode selected = RadialMenuProvider.super.setOption(stack, hand, holder, index);
        if (holder.level() instanceof ServerLevel level) {
            String anim = null;
            SoundEvent sound = null;
            if (selected == Mode.PHONE || selected == Mode.SPYGLASS) {
                if (old == Mode.PHONE || old == Mode.SPYGLASS)
                    anim = ANIMATION_OPEN;
                else {
                    anim = ANIMATION_RETRACT_AND_OPEN;
                    sound = MineraculousSoundEvents.CAT_STAFF_RETRACT.get();
                }
            } else if (old == Mode.PHONE || old == Mode.SPYGLASS) {
                anim = ANIMATION_CLOSE_AND_EXTEND;
                sound = MineraculousSoundEvents.CAT_STAFF_EXTEND.get();
            }
            if (anim != null)
                triggerAnim(holder, GeoItem.getOrAssignId(stack, level), CONTROLLER_EXTEND, anim);
            if (sound != null)
                level.playSound(null, holder.blockPosition(), sound, holder.getSoundSource(), 1.0F, 1.0F);
        }
        return selected;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    @Override
    public void onToggle(ItemStack stack, @Nullable Entity holder, Active active) {
        Mode mode = stack.get(MineraculousDataComponents.CAT_STAFF_MODE);
        if (holder != null) {
            String anim;
            SoundEvent sound;
            if (active.active()) {
                if (mode == Mode.PHONE || mode == Mode.SPYGLASS)
                    anim = ANIMATION_OPEN;
                else
                    anim = ANIMATION_EXTEND;
                sound = MineraculousSoundEvents.CAT_STAFF_EXTEND.get();
            } else {
                if (mode == Mode.PHONE || mode == Mode.SPYGLASS)
                    anim = ANIMATION_CLOSE;
                else
                    anim = ANIMATION_RETRACT;
                sound = MineraculousSoundEvents.CAT_STAFF_RETRACT.get();
            }
            triggerAnim(holder, GeoItem.getOrAssignId(stack, (ServerLevel) holder.level()), CONTROLLER_EXTEND, anim);
            holder.level().playSound(null, holder.blockPosition(), sound, holder.getSoundSource(), 1.0F, 1.0F);
        }
    }

    /**
     * Calculates the staff's tip in world space.
     * Used for travel and perch.
     * 
     * @param user     The entity using the staff.
     * @param sideways True if the staff should render next to the player's dominant hand.
     * @return Returns the position of the upward extremity in world space.
     */
    public static Vec3 staffTipStartup(Entity user, boolean sideways) {
        Vec3 userPosition = user.position();
        Vec2 horizontalFacing = MineraculousMathUtils.getHorizontalFacingVector(user.getYRot());
        Vec3 front = new Vec3(horizontalFacing.x, 0, horizontalFacing.y);
        Vec3 placement = sideways
                ? MineraculousMathUtils.UP.cross(front)
                        .scale((user instanceof Player player && player.getMainArm() == HumanoidArm.RIGHT) ? -1 : 1)
                        .add(front.scale(CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS))
                : front;
        placement = placement.scale(CatStaffItem.DISTANCE_BETWEEN_STAFF_AND_USER_IN_BLOCKS);
        double userHeight = user.getEyeHeight(Pose.STANDING);
        return new Vec3(
                userPosition.x + placement.x,
                userPosition.y + userHeight + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET,
                userPosition.z + placement.z);
    }

    /**
     * Calculates the staff's origin in world space.
     * Used for travel and perch.
     * 
     * @param user     The entity using the staff.
     * @param staffTip The position of the tip/
     * @return Returns the position of the downward extremity in world space.
     */
    public static Vec3 staffOriginStartup(Entity user, Vec3 staffTip) {
        double userY = user.getY();
        return new Vec3(staffTip.x, userY, staffTip.z);
    }

    /**
     * @param user The entity using the staff.
     * @return Returns the minimum length of the staff depending on the user's height
     */
    public static double getMinStaffLength(Entity user) {
        return user.getEyeHeight(Pose.STANDING) + CatStaffItem.STAFF_HEAD_ABOVE_USER_HEAD_OFFSET;
    }

    public enum Mode implements RadialMenuOption, StringRepresentable {
        BLOCK,
        PERCH,
        PHONE((stack, player) -> MineraculousConstants.Dependencies.TOMMYTECH.isLoaded()),
        SPYGLASS,
        THROW,
        TRAVEL;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
        public static final StreamCodec<ByteBuf, Mode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Mode::of, Mode::getSerializedName);

        private static final List<Mode> VALUES_LIST = new ReferenceArrayList<>(values());

        private final BiPredicate<ItemStack, Player> enabledPredicate;
        private final Component displayName;

        Mode() {
            this((stack, player) -> true);
        }

        Mode(BiPredicate<ItemStack, Player> enabledPredicate) {
            this.enabledPredicate = enabledPredicate;
            this.displayName = Component.translatable(MineraculousItems.CAT_STAFF.getId().toLanguageKey("mode", getSerializedName()));
        }

        @Override
        public Component displayName() {
            return displayName;
        }

        @Override
        public boolean isEnabled(ItemStack stack, Player holder) {
            return enabledPredicate.test(stack, holder);
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public static List<Mode> valuesList() {
            return VALUES_LIST;
        }

        public static Mode of(String name) {
            return valueOf(name.toUpperCase());
        }
    }
}
