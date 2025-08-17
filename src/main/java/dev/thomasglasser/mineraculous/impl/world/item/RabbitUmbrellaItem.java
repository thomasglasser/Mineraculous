package dev.thomasglasser.mineraculous.impl.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousTiers;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownRabbitUmbrella;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.client.renderer.item.DefaultedGeoItemRenderer;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
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
import net.minecraft.server.level.ServerLevel;
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

public class RabbitUmbrellaItem extends SwordItem implements GeoItem, ModeledItem, ProjectileItem, RadialMenuProvider<RabbitUmbrellaItem.Ability> {
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String ANIMATION_SHEATHE = "sheathe";
    public static final String ANIMATION_UNSHEATHE = "unsheathe";

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

    public RabbitUmbrellaItem(Properties properties) {
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
            else if (stack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY) == Ability.BLADE && !state.isCurrentAnimation(UNSHEATHE))
                return state.setAndContinue(BLADE_IDLE);
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_SHEATHE, SHEATH)
                .triggerableAnim(ANIMATION_UNSHEATHE, UNSHEATHE));
    }

    @Override
    public void createBewlrProvider(Consumer<BewlrProvider> provider) {
        provider.accept(new BewlrProvider() {
            private BlockEntityWithoutLevelRenderer bewlr;

            @Override
            public BlockEntityWithoutLevelRenderer getBewlr() {
                if (bewlr == null) bewlr = new DefaultedGeoItemRenderer<>(MineraculousItems.RABBIT_UMBRELLA.getId());
                return bewlr;
            }
        });
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new ThrownRabbitUmbrella(position.x(), position.y(), position.z(), level, itemStack);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (stack.has(MineraculousDataComponents.BLOCKING) && entity.getXRot() <= -75 && entity.getDeltaMovement().y <= 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, -0.1, entity.getDeltaMovement().z);
            entity.resetFallDistance();
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.has(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY.get())) {
            Ability ability = stack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY.get());
            if (ability == Ability.BLOCK || ability == Ability.THROW || ability == Ability.BLADE) {
                player.startUsingItem(hand);
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
        if (livingEntity instanceof Player player) {
            if (stack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY.get()) == Ability.THROW || stack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY.get()) == Ability.BLADE) {
                int i = this.getUseDuration(stack, livingEntity) - timeLeft;
                if (i >= 10) {
                    if (!level.isClientSide) {
                        ThrownRabbitUmbrella thrown = new ThrownRabbitUmbrella(level, livingEntity, stack);
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
        Ability ability = stack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY);
        if (ability == Ability.BLADE || ability == Ability.THROW)
            return UseAnim.SPEAR;
        return UseAnim.NONE;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        Ability ability = stack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY.get());
        return switch (ability) {
            case BLOCK -> itemAbility == ItemAbilities.SHIELD_BLOCK;
            case THROW -> itemAbility == ItemAbilities.TRIDENT_THROW;
            case null, default -> false;
        };
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (stack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY) == Ability.BLADE)
            return BLADE;
        return COVERED;
    }

    @Override
    public int getColor(ItemStack stack, InteractionHand hand, Player holder) {
        Level level = holder.level();
        int color = level.holderOrThrow(Miraculouses.RABBIT).value().color().getValue();
        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
        if (ownerId != null) {
            Entity owner = level.getEntities().get(ownerId);
            if (owner != null) {
                Holder<Miraculous> colorKey = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).getFirstTransformedIn(MiraculousTags.CAN_USE_RABBIT_UMBRELLA);
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
        return MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY;
    }

    @Override
    public Ability setOption(ItemStack stack, InteractionHand hand, Player holder, int index) {
        Ability old = stack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY);
        Ability selected = RadialMenuProvider.super.setOption(stack, hand, holder, index);
        if (holder.level() instanceof ServerLevel level) {
            UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
            if (ownerId != null) {
                Entity owner = level.getEntities().get(ownerId);
                if (owner != null) {
                    MiraculousesData miraculousesData = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                    MiraculousData storingData = miraculousesData.get(miraculousesData.getFirstTransformedIn(MiraculousTags.CAN_USE_RABBIT_UMBRELLA));
                    String anim = null;
                    if (selected == Ability.BLADE)
                        anim = ANIMATION_UNSHEATHE;
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
        PHONE((stack, player) -> Mineraculous.Dependencies.TOMMYTECH.isLoaded()),
        THROW;

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
            this.displayName = Component.translatable(MineraculousItems.RABBIT_UMBRELLA.getId().toLanguageKey("ability", getSerializedName()));
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
