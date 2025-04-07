package dev.thomasglasser.mineraculous.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.animations.PlayerAnimationUtil;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ClientboundCatStaffPerchPayload;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolPayload;
import dev.thomasglasser.mineraculous.network.ServerboundEquipToolPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetCatStaffAbilityPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetDeltaMovementPayload;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousMiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
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
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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

public class CatStaffItem extends SwordItem implements ModeledItem, GeoItem, ProjectileItem, ICurioItem {
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final ResourceLocation EXTENDED_PROPERTY_ID = Mineraculous.modLoc("extended");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String ANIMATION_BLOCK = "block";
    public static final String ANIMATION_EXTEND = "extend";
    public static final String ANIMATION_RETRACT = "retract";

    private static final RawAnimation EXTEND = RawAnimation.begin().thenPlay("misc.extend");
    private static final RawAnimation RETRACT = RawAnimation.begin().thenPlay("misc.retract");

    private static final ItemAttributeModifiers EXTENDED = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 15, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -1.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ENTITY_INTERACTION_RANGE_ID, 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected CatStaffItem(Properties pProperties) {
        super(MineraculousTiers.MIRACULOUS, pProperties
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_USE, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (!stack.has(MineraculousDataComponents.ACTIVE) && !state.isCurrentAnimation(RETRACT))
                return state.setAndContinue(DefaultAnimations.IDLE);
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_BLOCK, DefaultAnimations.ATTACK_BLOCK)
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
        if (entity instanceof Player player && !player.isUsingItem()) {
            if (level.isClientSide() && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                CatStaffItem.Ability stackAbility = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen()) {
                    if (MineraculousKeyMappings.ACTIVATE_TOOL.get().isDown()) {
                        boolean activate = !stack.has(MineraculousDataComponents.ACTIVE);
                        if (activate) {
                            stack.set(MineraculousDataComponents.ACTIVE, Unit.INSTANCE);
                        } else {
                            stack.remove(MineraculousDataComponents.ACTIVE);
                        }
                        TommyLibServices.NETWORK.sendToServer(new ServerboundActivateToolPayload(activate, hand, CONTROLLER_USE, activate ? ANIMATION_EXTEND : ANIMATION_RETRACT, activate ? MineraculousSoundEvents.CAT_STAFF_EXTEND : MineraculousSoundEvents.CAT_STAFF_RETRACT));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    } else if (MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown()) {
                        if (stack.has(MineraculousDataComponents.ACTIVE)) {
                            int color = level.holderOrThrow(MineraculousMiraculous.CAT).value().color().getValue();
                            ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
                            if (resolvableProfile != null) {
                                Player staffOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                                if (staffOwner != null) {
                                    ResourceKey<Miraculous> colorKey = staffOwner.getData(MineraculousAttachmentTypes.MIRACULOUS).getFirstKeyIn(MineraculousMiraculousTags.CAN_USE_CAT_STAFF, level);
                                    if (colorKey != null)
                                        color = level.holderOrThrow(colorKey).value().color().getValue();
                                }
                            }
                            MineraculousClientEvents.openToolWheel(color, stack, option -> {
                                if (option instanceof Ability ability) {
                                    stack.set(MineraculousDataComponents.CAT_STAFF_ABILITY.get(), ability);
                                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetCatStaffAbilityPayload(hand, ability));
                                }
                            }, Ability.values());
                        } else {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundEquipToolPayload(hand));
                        }
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    }
                }
                if (stackAbility == Ability.PERCH && stack.has(MineraculousDataComponents.ACTIVE) && player.getMainHandItem().is(MineraculousItems.CAT_STAFF)) {
                    float d = 0;
                    boolean k = false;
                    float length = player.getData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH).get();
                    boolean shouldNotFall = (groundRYClient == length);
                    if (MineraculousKeyMappings.WEAPON_DOWN_ARROW.get().isDown()) {
                        d -= 0.3f;
                        k = true;
                    }
                    if (MineraculousKeyMappings.WEAPON_UP_ARROW.get().isDown() && Math.abs(groundRYClient) < 64) {
                        d += 0.3f;
                        k = true;
                    }
                    if (k) {
                        Vec3 vec3 = new Vec3(0, d, 0);
                        player.setDeltaMovement(vec3);
                        player.hurtMarked = true;
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(vec3.toVector3f(), true));
                    } else if (shouldNotFall && perchTickClient > 30) {
                        player.setDeltaMovement(Vec3.ZERO);
                        player.hurtMarked = true;
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(Vec3.ZERO.toVector3f(), true));
                    }

                    if (staffPosition == Vec3.ZERO) {
                        float bodyAngle = -player.getYRot();
                        if (bodyAngle < 0) //simplify:
                            bodyAngle += 360.0f;
                        if (bodyAngle >= 360.0f)
                            bodyAngle -= 360.0f;
                        double cos = Math.cos(Math.toRadians(bodyAngle)); //z
                        double sin = Math.sin(Math.toRadians(bodyAngle)); //x
                        staffPosition = new Vec3(sin, 0, cos);
                        staffPosition = staffPosition.normalize();
                        staffPosition = staffPosition.scale(7d / 16d);
                        staffPosition = staffPosition.add(player.position()); //relative to 0 0 0
                    }
                    if (staffPosition != Vec3.ZERO) {
                        staffPosition = new Vec3(staffPosition.x, player.getY(), staffPosition.z);
                        Vec3 distance = new Vec3(staffPosition.x - player.getX(), staffPosition.y - player.getY(), staffPosition.z - player.getZ());
                        double ln = distance.length();
                        if (ln > 7d / 16d) {
                            player.setPos(player.position().add(distance.scale(ln - 7d / 16d)));
                        } else if (ln < 7d / 16d) {
                            player.setPos(player.position().add(distance.scale(7d / 16d - ln)));
                        }
                    }
                }
                //TODO rewrite this condition for left handed (and above)
                if (!(stackAbility == Ability.PERCH && stack.has(MineraculousDataComponents.ACTIVE)) || !player.getMainHandItem().is(MineraculousItems.CAT_STAFF)) {
                    staffPosition = Vec3.ZERO;
                }
                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
            }
            if (!level.isClientSide && entity instanceof ServerPlayer serverPlayer) {
                float length = player.getData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH).isPresent() ? player.getData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH).get() : 0f;
                CatStaffItem.Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);
                if (stack.has(MineraculousDataComponents.ACTIVE)) {
                    if (ability == Ability.TRAVEL && player.getCooldowns().isOnCooldown(stack.getItem()))
                        entity.resetFallDistance();
                    if (ability == Ability.PERCH) {
                        if (!catStaffPerchPerching.getOrDefault(entity.getUUID(), false)) {
                            player.setData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH, Optional.of(0f));
                            TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffPerchPayload(0, 0f, player.getUUID()), serverPlayer.server);
                            catStaffPerchPerching.put(entity.getUUID(), true);
                        }
                        //TICKING LOGIC:
                        int t = catStaffPerchTick.getOrDefault(entity.getUUID(), 0);
                        if (catStaffPerchPerching.getOrDefault(entity.getUUID(), false) && t > 10 && t < 30) {
                            if (player.getDeltaMovement().y >= -0.1)
                                player.setDeltaMovement(0, 0.8, 0);
                            else
                                player.setDeltaMovement(0, -player.getDeltaMovement().y, 0);
                            player.hurtMarked = true;
                        }
                        if (catStaffPerchPerching.getOrDefault(entity.getUUID(), false) && t <= 30) {
                            catStaffPerchTick.put(entity.getUUID(), t + 1);
                        }
                        if (t == 1) PlayerAnimationUtil.playAnimationToAllClients(player, "perch_press_button");
                        TommyLibServices.NETWORK.sendToClient(new ClientboundCatStaffPerchPayload(1, t, player.getUUID()), serverPlayer);

                        //JUST FOR THE RENDERER
                        TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffPerchPayload(3, t > 10 ? 1 : 0, player.getUUID()), serverPlayer.server);

                        //TODO work on this once kamilo does the rotating animations
                        /*
                        if (catStaffPerchInitialYAngle.getOrDefault(player.getUUID(), 1989f) == 1989) {
                            float rot = player.getYRot();
                            if (rot < 0) rot += 360;
                            catStaffPerchInitialYAngle.put(player.getUUID(), rot);
                        }
                        
                        float firstLimit = catStaffPerchInitialYAngle.get(player.getUUID()) + 47f;
                        float secondLimit = catStaffPerchInitialYAngle.get(player.getUUID()) - 47f;
                        
                        boolean ok = true;
                        if (firstLimit > 360 || secondLimit < 0) {
                            ok = false;
                            firstLimit -= firstLimit > 360 ? 360 : 0;
                            secondLimit += secondLimit > 0 ? 360 : 0;
                        }
                        float angle = player.getYRot();
                        if (angle < 0) angle += 360;
                        boolean isInLimit = ok ? (angle <= firstLimit && angle >= secondLimit) : (angle <= firstLimit || angle >= secondLimit);
                        if (!isInLimit) {
                            float a = angle - firstLimit;
                            float b = secondLimit - angle;
                            float newRot = catStaffPerchInitialYAngle.get(player.getUUID());
                            if (b > a) {
                                newRot = secondLimit > 180 ? secondLimit - 360 : secondLimit;
                            } else if (b <= a) {
                                newRot = firstLimit > 180 ? firstLimit - 360 : firstLimit;
                            }
                            //TommyLibServices.NETWORK.sendToClient(new ClientboundSetPlayerRotationPayload(player.getXRot(), player.getYRot(), catStaffPerchInitialYAngle.get(player.getUUID()), player.getUUID()), serverPlayer);
                        }*/

                        int y = entity.getBlockY();
                        while (level.getBlockState(new BlockPos(entity.getBlockX(), y, entity.getBlockZ())).isEmpty() && Math.abs(entity.getBlockY() - y) <= 64) {
                            y--;
                        }
                        y++;
                        catStaffPerchGroundRY.put(entity.getUUID(), (float) y - (float) entity.getY());
                        TommyLibServices.NETWORK.sendToClient(new ClientboundCatStaffPerchPayload(2, catStaffPerchGroundRY.getOrDefault(entity.getUUID(), 0f), player.getUUID()), serverPlayer);
                        //THIS MAKES THE STAFF EXTEND ITS LENGTH:
                        if (catStaffPerchGroundRY.getOrDefault(entity.getUUID(), 0f) < length) {
                            float newLength = length - 1f;
                            player.setData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH, Optional.of(newLength));
                            TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffPerchPayload(0, newLength, player.getUUID()), serverPlayer.server);
                        }
                        if (catStaffPerchGroundRY.getOrDefault(entity.getUUID(), 0f) > length) {
                            float newLength = catStaffPerchGroundRY.getOrDefault(entity.getUUID(), 0f);
                            player.setData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH, Optional.of(newLength));
                            TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffPerchPayload(0, newLength, player.getUUID()), serverPlayer.server);
                        }
                    }

                    if (ability != Ability.PERCH || !(player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                        catStaffPerchPerching.put(entity.getUUID(), false);
                        catStaffPerchTick.put(entity.getUUID(), 0);
                        catStaffPerchInitialYAngle.put(player.getUUID(), 1989f);
                        player.setData(MineraculousAttachmentTypes.CAT_STAFF_PERCH_LENGTH, Optional.of(0f));
                        TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffPerchPayload(0, 0f, player.getUUID()), serverPlayer.server);
                        TommyLibServices.NETWORK.sendToClient(new ClientboundCatStaffPerchPayload(1, 0f, player.getUUID()), serverPlayer);
                        TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffPerchPayload(3, 0, player.getUUID()), serverPlayer.server);
                    }
                }
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    private static HashMap<UUID, Float> catStaffPerchGroundRY = new HashMap<>();
    private static HashMap<UUID, Float> catStaffPerchInitialYAngle = new HashMap<>();
    private static HashMap<UUID, Boolean> catStaffPerchPerching = new HashMap<>();
    private static HashMap<UUID, Integer> catStaffPerchTick = new HashMap<>();
    public static HashMap<UUID, Boolean> catStaffPerchRender = new HashMap<>();
    public static int perchTickClient = 0;
    public static float groundRYClient = 0;
    public static Vec3 staffPosition = Vec3.ZERO;

    public static int getPerchTick(UUID key) {
        return catStaffPerchTick.getOrDefault(key, 0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!stack.has(MineraculousDataComponents.ACTIVE))
            return InteractionResultHolder.fail(stack);
        if (stack.has(MineraculousDataComponents.CAT_STAFF_ABILITY)) {
            Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get());
            if (ability == Ability.BLOCK || ability == Ability.THROW)
                pPlayer.startUsingItem(pHand);
            else if (ability == Ability.TRAVEL) {
                if (level instanceof ServerLevel) {
                    pPlayer.setDeltaMovement(pPlayer.getLookAngle().scale(3));
                    pPlayer.hurtMarked = true;
                    pPlayer.getCooldowns().addCooldown(stack.getItem(), 10);
                }
            }
            if (level instanceof ServerLevel serverLevel) {
                long animId = GeoItem.getOrAssignId(stack, serverLevel);
                switch (ability) {
                    case BLOCK -> triggerAnim(pPlayer, animId, CONTROLLER_USE, ANIMATION_BLOCK);
                    case null, default -> {}
                }
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, pPlayer, pHand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get()) == Ability.BLOCK && remainingUseDuration % 10 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.GENERIC_SHIELD.get());
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            long animId = GeoItem.getOrAssignId(stack, serverLevel);
            if (stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get()) == Ability.BLOCK) {
                stopTriggeredAnim(entity, animId, CONTROLLER_USE, ANIMATION_BLOCK);
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get());
        if (entityLiving instanceof Player player && ability == Ability.THROW) {
            int i = this.getUseDuration(stack, entityLiving) - timeLeft;
            if (i >= 10) {
                if (!level.isClientSide) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(entityLiving.getUsedItemHand()));
                    ThrownCatStaff thrown = new ThrownCatStaff(entityLiving, level, stack);
                    thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
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

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get());
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
            case null, default -> false;
        };
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (stack.has(MineraculousDataComponents.ACTIVE))
            return EXTENDED;
        return super.getDefaultAttributeModifiers(stack);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new ThrownCatStaff(pos.x(), pos.y(), pos.z(), level, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return canEquip(stack);
    }

    public boolean canEquip(ItemStack stack) {
        return !stack.has(MineraculousDataComponents.ACTIVE);
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        if (canEquip(stack)) {
            return ICurioItem.super.getSlotsTooltip(tooltips, context, stack);
        }
        return List.of();
    }

    public enum Ability implements RadialMenuOption, StringRepresentable {
        BLOCK,
        PERCH,
        THROW,
        TRAVEL;

        public static final Codec<Ability> CODEC = StringRepresentable.fromEnum(Ability::values);
        public static final StreamCodec<ByteBuf, Ability> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Ability::of, Ability::getSerializedName);

        private final String translationKey;

        Ability() {
            this.translationKey = MineraculousItems.CAT_STAFF.getId().toLanguageKey("ability", getSerializedName());
        }

        @Override
        public String translationKey() {
            return translationKey;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public static Ability of(String name) {
            return valueOf(name.toUpperCase());
        }
    }
}
