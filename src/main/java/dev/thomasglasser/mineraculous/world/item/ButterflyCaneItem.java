package dev.thomasglasser.mineraculous.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.renderer.item.DefaultedGeoItemRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownButterflyCane;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.item.component.ResolvableProfile;
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

public class ButterflyCaneItem extends SwordItem implements GeoItem, ModeledItem, ProjectileItem, RadialMenuProvider<ButterflyCaneItem.Ability> {
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String ANIMATION_OPEN = "open";
    public static final String ANIMATION_CLOSE = "close";
    public static final String ANIMATION_SHEATHE = "sheathe";
    public static final String ANIMATION_UNSHEATHE = "unsheathe";
    public static final String TAG_STORED_KAMIKO = "StoredKamiko";

    public static final RawAnimation BLADE_IDLE = RawAnimation.begin().thenPlay("misc.blade_idle");

    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("misc.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("misc.close");
    private static final RawAnimation SHEATH = RawAnimation.begin().thenPlay("attack.sheathe");
    private static final RawAnimation UNSHEATHE = RawAnimation.begin().thenPlay("attack.unsheathe");

    private static final ItemAttributeModifiers BLADE = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 15, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, 0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ENTITY_INTERACTION_RANGE_ID, 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    private static final ItemAttributeModifiers COVERED = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected ButterflyCaneItem(Properties properties) {
        super(MineraculousTiers.MIRACULOUS, properties
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_USE, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack.has(MineraculousDataComponents.BLOCKING))
                return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
            else if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == Ability.BLADE && !state.isCurrentAnimation(UNSHEATHE))
                return state.setAndContinue(BLADE_IDLE);
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_OPEN, OPEN)
                .triggerableAnim(ANIMATION_CLOSE, CLOSE)
                .triggerableAnim(ANIMATION_SHEATHE, SHEATH)
                .triggerableAnim(ANIMATION_UNSHEATHE, UNSHEATHE));
    }

    @Override
    public void createBewlrProvider(Consumer<BewlrProvider> provider) {
        provider.accept(new BewlrProvider() {
            private BlockEntityWithoutLevelRenderer bewlr;

            @Override
            public BlockEntityWithoutLevelRenderer getBewlr() {
                if (bewlr == null) bewlr = new DefaultedGeoItemRenderer<>(MineraculousItems.BUTTERFLY_CANE.getId());
                return bewlr;
            }
        });
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
        if (entity instanceof Player player && !player.isUsingItem()) {
            if (level.isClientSide() && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                // TODO: Fix
                CompoundTag playerData = /*TommyLibServices.ENTITY.getPersistentData(entity)*/new CompoundTag();
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen() && MineraculousKeyMappings.OPEN_ITEM_RADIAL_MENU.isDown()) {
                    int color = level.holderOrThrow(Miraculouses.BUTTERFLY).value().color().getValue();
                    ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
                    if (resolvableProfile != null) {
                        Player caneOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                        if (caneOwner != null) {
                            ResourceKey<Miraculous> colorKey = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES).getFirstTransformedKeyIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE, level);
                            if (colorKey != null)
                                color = level.holderOrThrow(colorKey).value().color().getValue();
                        }
                    }
//                    MineraculousClientEvents.openToolWheel(color, stack, option -> {
//                        if (option instanceof Ability ability) {
//                            TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneAbilityPayload(hand, ability));
//                            stack.set(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get(), ability);
//                        }
//                    }, Arrays.stream(Ability.values()).filter(ability -> {
//                        if (ability == Ability.KAMIKO_STORE)
//                            return stack.has(DataComponents.PROFILE);
//                        return true;
//                    }).toArray(Ability[]::new));
                    playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
//                    TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
                }
            }
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
        if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == Ability.KAMIKO_STORE && interactionTarget instanceof Kamiko kamiko && resolvableProfile != null) {
            Player caneOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
            if (caneOwner != null) {
                MiraculousesData miraculousesData = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                ResourceKey<Miraculous> storingKey = miraculousesData.getFirstTransformedKeyIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE, player.level());
                MiraculousData storingData = miraculousesData.get(storingKey);
                if (storingData != null && !storingData.extraData().contains(TAG_STORED_KAMIKO)) {
                    if (player.level() instanceof ServerLevel serverLevel) {
                        long animId = GeoItem.getOrAssignId(stack, serverLevel);
                        triggerAnim(player, animId, CONTROLLER_USE, ANIMATION_CLOSE);
                        storingData.extraData().put(TAG_STORED_KAMIKO, kamiko.saveWithoutId(new CompoundTag()));
                        miraculousesData.put(caneOwner, storingKey, storingData, true);
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
        if (stack.has(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get())) {
            Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get());
            ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
            if (ability == Ability.BLOCK || ability == Ability.THROW || ability == Ability.BLADE) {
                player.startUsingItem(hand);
            } else if (resolvableProfile != null) {
                Player caneOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                if (caneOwner != null) {
                    MiraculousesData miraculousesData = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                    ResourceKey<Miraculous> storingKey = miraculousesData.getFirstTransformedKeyIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE, player.level());
                    MiraculousData storingData = miraculousesData.get(storingKey);
                    if (ability == Ability.KAMIKO_STORE && level instanceof ServerLevel serverLevel && storingData != null && storingData.extraData().contains(TAG_STORED_KAMIKO)) {
                        Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().create(serverLevel);
                        if (kamiko != null) {
                            kamiko.load(storingData.extraData().getCompound(TAG_STORED_KAMIKO));
                            kamiko.setPos(player.position().add(0, 1, 0));
                            serverLevel.addFreshEntity(kamiko);
                            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), CONTROLLER_USE, ANIMATION_OPEN);
                            storingData.extraData().remove(TAG_STORED_KAMIKO);
                            miraculousesData.put(caneOwner, storingKey, storingData, true);
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
            livingEntity.playSound(MineraculousSoundEvents.GENERIC_SHIELD.get());
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof Player player) {
            if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()) == Ability.THROW || stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()) == Ability.BLADE) {
                int i = this.getUseDuration(stack, livingEntity) - timeLeft;
                if (i >= 10) {
                    if (!level.isClientSide) {
                        ThrownButterflyCane thrown = new ThrownButterflyCane(livingEntity, level, stack);
                        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
                        if (player.hasInfiniteMaterials()) {
                            thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        level.addFreshEntity(thrown);
                        level.playSound(null, thrown, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        if (!player.hasInfiniteMaterials()) {
                            player.getInventory().removeItem(stack);
                        }
                    }

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
        if (stack.has(MineraculousDataComponents.BLOCKING))
            return UseAnim.BLOCK;
        Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY);
        if (ability == Ability.BLADE || ability == Ability.THROW)
            return UseAnim.SPEAR;
        return UseAnim.NONE;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get());
        return switch (ability) {
            case BLOCK -> itemAbility == ItemAbilities.SHIELD_BLOCK;
            case null, default -> false;
        };
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == Ability.BLADE)
            return BLADE;
        return COVERED;
    }

    @Override
    public int getColor(ItemStack stack, InteractionHand hand, Player holder) {
        Level level = holder.level();
        int color = level.holderOrThrow(Miraculouses.BUTTERFLY).value().color().getValue();
        ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
        if (resolvableProfile != null) {
            Player owner = level.getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
            if (owner != null) {
                ResourceKey<Miraculous> colorKey = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).getFirstTransformedKeyIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE, level);
                if (colorKey != null)
                    color = level.holderOrThrow(colorKey).value().color().getValue();
            }
        }
        return color;
    }

    @Override
    public List<Ability> getOptions(ItemStack stack, InteractionHand hand, Player holder) {
        if (stack.has(DataComponents.PROFILE))
            return Ability.valuesList();
        return Ability.unpoweredValuesList();
    }

    @Override
    public Supplier<DataComponentType<Ability>> getComponentType(ItemStack stack, InteractionHand hand, Player holder) {
        return MineraculousDataComponents.BUTTERFLY_CANE_ABILITY;
    }

    @Override
    public Ability setOption(ItemStack stack, InteractionHand hand, int index, Player holder) {
        Ability old = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY);
        Ability selected = RadialMenuProvider.super.setOption(stack, hand, index, holder);
        if (holder.level() instanceof ServerLevel level) {
            ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
            if (resolvableProfile != null) {
                Player owner = level.getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                if (owner != null) {
                    MiraculousesData miraculousesData = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                    MiraculousData storingData = miraculousesData.get(miraculousesData.getFirstTransformedKeyIn(MiraculousTags.CAN_USE_BUTTERFLY_CANE, level));
                    String anim = null;
                    if (selected == Ability.BLADE)
                        anim = ANIMATION_UNSHEATHE;
                    else if (selected == Ability.KAMIKO_STORE && storingData != null && !storingData.extraData().contains(TAG_STORED_KAMIKO))
                        anim = ANIMATION_OPEN;
                    else if (old == Ability.KAMIKO_STORE && storingData != null && !storingData.extraData().contains(TAG_STORED_KAMIKO))
                        anim = ANIMATION_CLOSE;
                    else if (old == Ability.BLADE)
                        anim = ANIMATION_SHEATHE;
                    if (anim != null) {
                        triggerAnim(holder, GeoItem.getOrAssignId(stack, level), CONTROLLER_USE, anim);
                    }
                }
            } else {
                String anim = null;
                if (selected == Ability.BLADE)
                    anim = ANIMATION_UNSHEATHE;
                else if (old == Ability.BLADE)
                    anim = ANIMATION_SHEATHE;
                if (anim != null) {
                    triggerAnim(holder, GeoItem.getOrAssignId(stack, level), CONTROLLER_USE, anim);
                }
            }
        }
        return selected;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    public enum Ability implements RadialMenuOption, StringRepresentable {
        BLADE,
        BLOCK,
        KAMIKO_STORE,
        THROW;

        public static final Codec<Ability> CODEC = StringRepresentable.fromEnum(Ability::values);
        public static final StreamCodec<ByteBuf, Ability> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Ability::of, Ability::getSerializedName);

        private static final List<Ability> VALUES_LIST = new ReferenceArrayList<>(values());
        private static final List<Ability> UNPOWERED_VALUES_LIST = new ReferenceArrayList<>(Arrays.asList(BLADE, BLOCK, THROW));

        private final String translationKey;

        Ability() {
            this.translationKey = MineraculousItems.BUTTERFLY_CANE.getId().toLanguageKey("ability", getSerializedName());
        }

        @Override
        public String translationKey() {
            return translationKey;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public static List<Ability> valuesList() {
            return VALUES_LIST;
        }

        public static List<Ability> unpoweredValuesList() {
            return UNPOWERED_VALUES_LIST;
        }

        public static Ability of(String name) {
            return valueOf(name.toUpperCase());
        }
    }
}
