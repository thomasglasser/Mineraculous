package dev.thomasglasser.mineraculous.impl.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.projectile.ItemBreakingQuicklyReturningThrownSword;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousTiers;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.api.world.item.component.ActiveSettings;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.impl.network.ServerboundEquipToolPayload;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffPerchHandler;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffTravelHandler;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelingCatStaffData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
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
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class CatStaffItem extends SwordItem implements ModeledItem, GeoItem, ProjectileItem, ICurioItem, RadialMenuProvider<CatStaffItem.Ability> {
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String CONTROLLER_EXTEND = "extend_controller";
    public static final String ANIMATION_EXTEND = "extend";
    public static final String ANIMATION_RETRACT = "retract";

    public static final ActiveSettings ACTIVE_SETTINGS = new ActiveSettings(
            Optional.of(CatStaffItem.CONTROLLER_EXTEND),
            Optional.of(CatStaffItem.ANIMATION_EXTEND),
            Optional.of(CatStaffItem.ANIMATION_RETRACT),
            Optional.of(MineraculousSoundEvents.CAT_STAFF_EXTEND),
            Optional.of(MineraculousSoundEvents.CAT_STAFF_RETRACT));

    private static final RawAnimation EXTEND = RawAnimation.begin().thenPlay("misc.extend");
    private static final RawAnimation RETRACT = RawAnimation.begin().thenPlay("misc.retract");

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
                if (stack.get(MineraculousDataComponents.CARRIER) != null) {
                    int carrierID = stack.get(MineraculousDataComponents.CARRIER);
                    Entity entity = ClientUtils.getEntityById(carrierID);
                    if (entity instanceof LivingEntity livingEntity) {
                        boolean travelEligible = livingEntity.getUseItem() == stack && !livingEntity.onGround() && stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == Ability.TRAVEL && !livingEntity.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF).traveling();
                        if (stack.has(MineraculousDataComponents.BLOCKING) || travelEligible) {
                            return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
                        }
                    }
                }
            }
            return PlayState.STOP;
        }));
        controllers.add(new AnimationController<>(this, CONTROLLER_EXTEND, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack != null && !stack.getOrDefault(MineraculousDataComponents.ACTIVE, false) && !state.isCurrentAnimation(RETRACT)) {
                return state.setAndContinue(DefaultAnimations.IDLE);
            }
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_EXTEND, EXTEND)
                .triggerableAnim(ANIMATION_RETRACT, RETRACT));
    }

    @Override
    public void createBewlrProvider(Consumer<BewlrProvider> provider) {
        provider.accept(new BewlrProvider() {
            private BlockEntityWithoutLevelRenderer bewlr;

            @Override
            public BlockEntityWithoutLevelRenderer getBewlr() {
                if (bewlr == null) bewlr = new CatStaffRenderer();
                return bewlr;
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof LivingEntity livingEntity) {
            boolean isActive = stack.getOrDefault(MineraculousDataComponents.ACTIVE, false);
            if (isActive) {
                boolean inHand = livingEntity.getMainHandItem() == stack || livingEntity.getOffhandItem() == stack;
                Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);
                if (ability != null) {
                    PerchingCatStaffData perchingData = livingEntity.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
                    TravelingCatStaffData travelingData = livingEntity.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF);
                    if (inHand) {
                        switch (ability) {
                            case PERCH -> CatStaffPerchHandler.tick(level, livingEntity);
                            case TRAVEL -> CatStaffTravelHandler.tick(stack, level, livingEntity);
                            default -> {
                                if (!level.isClientSide) {
                                    if (perchingData != PerchingCatStaffData.emptyData)
                                        PerchingCatStaffData.remove(livingEntity, true);
                                    if (travelingData != TravelingCatStaffData.emptyData)
                                        TravelingCatStaffData.remove(livingEntity, true);
                                }
                            }
                        }
                        MineraculousItemUtils.checkHelicopterSlowFall(stack, entity);
                    } else {
                        if (!level.isClientSide) {
                            if (perchingData != PerchingCatStaffData.emptyData)
                                PerchingCatStaffData.remove(livingEntity, true);
                            if (travelingData != TravelingCatStaffData.emptyData)
                                TravelingCatStaffData.remove(livingEntity, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pHand) {
        ItemStack stack = player.getItemInHand(pHand);
        if (!stack.getOrDefault(MineraculousDataComponents.ACTIVE, false))
            return InteractionResultHolder.fail(stack);
        if (stack.has(MineraculousDataComponents.CAT_STAFF_ABILITY)) {
            Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);
            if (ability == Ability.BLOCK || ability == Ability.THROW || ability == Ability.PERCH || ability == Ability.TRAVEL)
                player.startUsingItem(pHand);
            if (ability == Ability.TRAVEL)
                CatStaffTravelHandler.init(level, player);
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, pHand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        boolean travelEligible = !livingEntity.onGround() && stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == Ability.TRAVEL && !livingEntity.getData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF).traveling() && stack.getUseDuration(livingEntity) - remainingUseDuration > 1;
        if ((stack.has(MineraculousDataComponents.BLOCKING) || travelEligible) && remainingUseDuration % 10 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.GENERIC_SPIN.get());
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get());
        if (ability == Ability.THROW) {
            int i = this.getUseDuration(stack, entityLiving) - timeLeft;
            if (i >= 10) {
                if (!level.isClientSide) {
                    stack.hurtAndBreak(1, entityLiving, LivingEntity.getSlotForHand(entityLiving.getUsedItemHand()));
                    ItemBreakingQuicklyReturningThrownSword thrown = new ThrownCatStaff(level, entityLiving, stack);
                    thrown.shootFromRotation(entityLiving, entityLiving.getXRot(), entityLiving.getYRot(), 0.0F, 2.5F, 1.0F);
                    if (entityLiving.hasInfiniteMaterials()) {
                        thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    }

                    level.addFreshEntity(thrown);
                    level.playSound(null, thrown, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (entityLiving instanceof Player player) {
                        if (!player.hasInfiniteMaterials()) {
                            player.getInventory().removeItem(stack);
                        }
                        level.addFreshEntity(thrown);
                        level.playSound(null, thrown, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        if (!player.hasInfiniteMaterials()) {
                            player.getInventory().removeItem(stack);
                        }
                        player.awardStat(Stats.ITEM_USED.get(this));
                    }
                }
            }
        } else if (ability == Ability.PERCH) {
            PerchingCatStaffData perchingCatStaffData = entityLiving.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
            CatStaffPerchHandler.itemUsed(level, entityLiving, perchingCatStaffData);
            if (entityLiving instanceof Player player) {
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);
        return switch (ability) {
            case BLOCK -> UseAnim.BLOCK;
            case THROW -> UseAnim.SPEAR;
            case null, default -> UseAnim.NONE;
        };
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get());
        return switch (ability) {
            case BLOCK -> itemAbility == ItemAbilities.SHIELD_BLOCK;
            case THROW -> itemAbility == ItemAbilities.TRIDENT_THROW;
            case null, default -> false;
        };
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (stack.getOrDefault(MineraculousDataComponents.ACTIVE, false))
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
        return !stack.getOrDefault(MineraculousDataComponents.ACTIVE, false);
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
        return stack.getOrDefault(MineraculousDataComponents.ACTIVE, false);
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
    public List<Ability> getOptions(ItemStack stack, InteractionHand hand, Player holder) {
        return Ability.valuesList();
    }

    @Override
    public Supplier<DataComponentType<Ability>> getComponentType(ItemStack stack, InteractionHand hand, Player holder) {
        return MineraculousDataComponents.CAT_STAFF_ABILITY;
    }

    @Override
    public boolean handleSecondaryKeyBehavior(ItemStack stack, InteractionHand hand, Player holder) {
        TommyLibServices.NETWORK.sendToServer(new ServerboundEquipToolPayload(hand));
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    public enum Ability implements RadialMenuOption, StringRepresentable {
        BLOCK,
        PERCH,
        PHONE((stack, player) -> /*Mineraculous.Dependencies.TOMMYTECH.isLoaded()*/true),
        THROW,
        TRAVEL;

        public static final Codec<Ability> CODEC = StringRepresentable.fromEnum(Ability::values);
        public static final StreamCodec<ByteBuf, Ability> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Ability::of, Ability::getSerializedName);

        private static final List<Ability> VALUES_LIST = new ReferenceArrayList<>(values());

        private final BiPredicate<ItemStack, Player> enabledPredicate;
        private final Component displayName;

        Ability() {
            this((stack, player) -> true);
        }

        Ability(BiPredicate<ItemStack, Player> enabledPredicate) {
            this.enabledPredicate = enabledPredicate;
            this.displayName = Component.translatable(MineraculousItems.CAT_STAFF.getId().toLanguageKey("ability", getSerializedName()));
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

        public static List<Ability> valuesList() {
            return VALUES_LIST;
        }

        public static Ability of(String name) {
            return valueOf(name.toUpperCase());
        }
    }
}
