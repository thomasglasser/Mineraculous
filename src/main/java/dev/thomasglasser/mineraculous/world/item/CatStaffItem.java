package dev.thomasglasser.mineraculous.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.animations.MineraculousPlayerAnimationUtil;
import dev.thomasglasser.mineraculous.client.animations.MineraculousPlayerAnimations;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolPayload;
import dev.thomasglasser.mineraculous.network.ServerboundEquipToolPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetCatStaffAbilityPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetDeltaMovementPayload;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousMiraculousTags;
import dev.thomasglasser.mineraculous.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.mineraculous.world.level.storage.PerchCatStaffData;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
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
import org.joml.Vector3f;
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

    public static float PERCH_STAFF_DISTANCE = 7f / 16f;

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && !player.isUsingItem()) {
            if (level.isClientSide() && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
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
                if (stack.has(MineraculousDataComponents.ACTIVE) && stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == Ability.PERCH && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                    PerchCatStaffData perchData = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF);
                    boolean isFalling = perchData.isFalling();
                    float length = perchData.length();
                    float groundRYClient = perchData.yGroundLevel(); //relative to client
                    int perchTickClient = perchData.tick();
                    if (!isFalling) {
                        float d = 0;
                        boolean k = false;
                        boolean shouldNotFall = (groundRYClient == length);
                        if (MineraculousKeyMappings.WEAPON_DOWN_ARROW.get().isDown()) {
                            d -= 0.3f;
                            k = true;
                        }
                        if (MineraculousKeyMappings.WEAPON_UP_ARROW.get().isDown() && Math.abs(groundRYClient) < 64) {
                            d += 0.3f;
                            k = true;
                        }
                        if (perchTickClient > 30) {
                            if (k) {
                                Vec3 vec3 = new Vec3(0, d, 0);
                                player.setDeltaMovement(vec3);
                                player.hurtMarked = true;
                                TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(vec3.toVector3f(), true));
                            } else if (shouldNotFall) {
                                player.setDeltaMovement(Vec3.ZERO);
                                player.hurtMarked = true;
                                TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(Vec3.ZERO.toVector3f(), true));
                            }
                        } else {
                            Vec3 vec3 = new Vec3(0, player.getDeltaMovement().y, 0);
                            player.setDeltaMovement(vec3);
                        }
                    }
                    constrainPerchMovement(player, stack);
                }
                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
            }
            if (!level.isClientSide) {
                CatStaffItem.Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);
                if (stack.has(MineraculousDataComponents.ACTIVE)) {
                    if (ability == Ability.TRAVEL && player.getCooldowns().isOnCooldown(stack.getItem()))
                        entity.resetFallDistance();
                    if (ability == Ability.PERCH && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                        PerchCatStaffData perchData = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF);
                        float length = perchData.length();
                        float catStaffPerchGroundRY = perchData.yGroundLevel();
                        Vector3f initPos = perchData.initPos();
                        float initRot;
                        boolean catStaffPerchPerching = perchData.startEdge();
                        boolean isFalling = perchData.isFalling();
                        float yBeforeFalling = perchData.yBeforeFalling();
                        if (!catStaffPerchPerching) {
                            catStaffPerchPerching = true;
                            length = 0f;
                            initRot = player.getYRot();
                            if (initRot < 0) //simplify:
                                initRot += 360.0f;
                            if (initRot >= 360.0f)
                                initRot -= 360.0f;
                            double cos = Math.cos(Math.toRadians(initRot)); //z
                            double sin = -Math.sin(Math.toRadians(initRot)); //x
                            Vec3 direction = new Vec3(sin, 0, cos);
                            Vec3 playerPos = new Vec3(player.getX(), 0, player.getZ());
                            direction = direction.normalize();
                            direction = direction.scale(PERCH_STAFF_DISTANCE);
                            direction = direction.add(playerPos);
                            initPos = new Vector3f((float) direction.x, initRot, (float) direction.z);
                            isFalling = false;
                        }

                        //TICKING LOGIC:
                        int t = perchData.tick();
                        if (t > 10 && t < 30) {
                            if (player.getDeltaMovement().y >= -0.1)
                                player.setDeltaMovement(0, 0.8, 0);
                            else
                                player.setDeltaMovement(0, -player.getDeltaMovement().y, 0);
                            player.hurtMarked = true;
                        }
                        if (t <= 30) {
                            t = t + 1;
                        }

                        //JUST FOR THE RENDERER
                        boolean nRender = t > 10;

                        if (!isFalling) {
                            //GROUND DETECTION
                            int y = entity.getBlockY();
                            while (level.getBlockState(new BlockPos(entity.getBlockX(), y, entity.getBlockZ())).isEmpty() && Math.abs(entity.getBlockY() - y) <= 64) {
                                y--;
                            }
                            y++;
                            catStaffPerchGroundRY = (float) y - (float) entity.getY();
                            yBeforeFalling = (float) player.getY();

                            //PLAY ANIMATIONS
                            playPlayerAnimations(player, t, initPos);

                            //THIS MAKES THE STAFF EXTEND ITS LENGTH:
                            if (catStaffPerchGroundRY < length) {
                                length = length - 1f;
                            }
                            if (catStaffPerchGroundRY > length) {
                                length = catStaffPerchGroundRY;
                            }

                            //SAVING DATA
                            PerchCatStaffData newPerchData = new PerchCatStaffData(length, catStaffPerchGroundRY, catStaffPerchPerching, t, nRender, initPos, isFalling, yBeforeFalling, perchData.initialFallDirection());
                            player.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, newPerchData);
                            newPerchData.save(player, true);
                        } else {
                            MineraculousPlayerAnimationUtil.sendAnimationToAllClients(
                                    player,
                                    MineraculousPlayerAnimations.NULL,
                                    MineraculousPlayerAnimationUtil.PlayerAnimationActions.STOP);
                        }
                    } else { //RESET
                        player.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, new PerchCatStaffData());
                        PerchCatStaffData.remove(player, true);
                        MineraculousPlayerAnimationUtil.sendAnimationToAllClients(
                                player,
                                MineraculousPlayerAnimations.NULL,
                                MineraculousPlayerAnimationUtil.PlayerAnimationActions.STOP);
                    }
                }
            }
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    private static void playPlayerAnimations(Player player, int tick, Vector3f initPos) {
        Direction fromPS = fromDelta(initPos.x - player.getX(), initPos.z - player.getZ());
        Direction front = Direction.fromYRot(MineraculousMathUtils.normalizeDegreesLikeEntityRot(player.getPreciseBodyRotation(0)));
        int difference = fromDirectionToInt(fromPS) - fromDirectionToInt(front);
        if (tick > 10) {
            String animationToPlay = switch (difference) {
                case 3, -1 -> MineraculousPlayerAnimations.CAT_STAFF_PERCH_ROTATION_LEFT;
                case 2, -2 -> MineraculousPlayerAnimations.CAT_STAFF_PERCH_ROTATION_BACK;
                case 1, -3 -> MineraculousPlayerAnimations.CAT_STAFF_PERCH_ROTATION_RIGHT;
                case 0 -> MineraculousPlayerAnimations.CAT_STAFF_PERCH_ROTATION_FRONT;
                default -> MineraculousPlayerAnimations.NULL;
            };
            MineraculousPlayerAnimationUtil.sendAnimationToAllClients(
                    player,
                    animationToPlay,
                    MineraculousPlayerAnimationUtil.PlayerAnimationActions.PLAY_HOLD_ON_FIRST_FRAME);
        }
        if (tick == 1) {
            MineraculousPlayerAnimationUtil.sendAnimationToAllClients(
                    player,
                    MineraculousPlayerAnimations.CAT_STAFF_PERCH_START,
                    MineraculousPlayerAnimationUtil.PlayerAnimationActions.PLAY_ONCE);
        }
    }

    private static void constrainPerchMovement(Player player, ItemStack stack) {
        PerchCatStaffData perchCatStaffData = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF);
        if (perchCatStaffData.isFalling()) {
            float yBeforeFalling = perchCatStaffData.yBeforeFalling();
            float length = perchCatStaffData.length();
            Vector3f staffOrigin = new Vector3f(perchCatStaffData.initPos().x, yBeforeFalling + length, perchCatStaffData.initPos().z);
            Vec3 fromPlayerToStaff = new Vec3(staffOrigin.x - player.getX(), staffOrigin.y - player.getY(), staffOrigin.z - player.getZ());
            length = -length;
            if (fromPlayerToStaff.length() < length) {
                Vec3 constrain = new Vec3(fromPlayerToStaff.toVector3f());
                constrain = constrain.normalize();
                constrain = constrain.scale(fromPlayerToStaff.length() - length);
                constrain = constrain.add(player.getX(), player.getY(), player.getZ());
                player.setPos(constrain);

                Vec3 towards = new Vec3(perchCatStaffData.initialFallDirection());
                towards = MineraculousMathUtils.projectOnCircle(fromPlayerToStaff, towards);
                towards = towards.normalize();
                player.setDeltaMovement(towards);
                player.hurtMarked = true;
            }
            boolean jump = Minecraft.getInstance().player.input.jumping;
            if (jump) {
                player.setDeltaMovement(player.getDeltaMovement().x, 1.5, player.getDeltaMovement().z);
            }
            //FELL CANCEL
            if (fromPlayerToStaff.length() > length + 1) {
                stack.set(MineraculousDataComponents.CAT_STAFF_ABILITY.get(), Ability.BLOCK);
                InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetCatStaffAbilityPayload(hand, Ability.BLOCK));
            }
        } else {
            Vector3f staffPosition = new Vector3f(perchCatStaffData.initPos());
            staffPosition = new Vector3f(staffPosition.x, 0, staffPosition.z);
            Vec3 fromPlayerToStaff = new Vec3(staffPosition.x - player.getX(), 0, staffPosition.z - player.getZ());
            if (player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF).startEdge()) {
                if (fromPlayerToStaff.length() > PERCH_STAFF_DISTANCE) {
                    Vec3 constrain = new Vec3((double) staffPosition.x - player.getX(), 0, (double) staffPosition.z - player.getZ());
                    constrain = constrain.normalize();
                    constrain = constrain.scale(fromPlayerToStaff.length() - PERCH_STAFF_DISTANCE);
                    constrain = constrain.add(player.getX(), player.getY(), player.getZ());
                    player.setPos(constrain);
                }
                if (fromPlayerToStaff.length() < PERCH_STAFF_DISTANCE) {
                    Vec3 constrain = new Vec3((double) staffPosition.x - player.getX(), 0, (double) staffPosition.z - player.getZ());
                    constrain = constrain.normalize();
                    constrain = constrain.scale(fromPlayerToStaff.length() - PERCH_STAFF_DISTANCE);
                    constrain = constrain.add(player.getX(), player.getY(), player.getZ());
                    player.setPos(constrain);
                }
            }
            int tick = perchCatStaffData.tick();
            if (tick > 30 && Minecraft.getInstance().player != null && (Minecraft.getInstance().player.input.up || Minecraft.getInstance().player.input.down || Minecraft.getInstance().player.input.left || Minecraft.getInstance().player.input.right)) {
                boolean front = Minecraft.getInstance().player.input.up;
                boolean back = Minecraft.getInstance().player.input.down;
                boolean left = Minecraft.getInstance().player.input.left;
                boolean right = Minecraft.getInstance().player.input.right;
                Vec3 staffPositionRelativeToThePlayer = new Vec3(staffPosition.x - player.getX(), 0, staffPosition.z - player.getZ());
                Vec3 movement = new Vec3(0f, 0f, 0f);
                Vec3 direction = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z).normalize();
                if (front) {
                    movement = movement.add(direction);
                }
                if (back) {
                    Vec3 down = direction;
                    down = down.scale(-1d);
                    down = down.normalize();
                    movement = movement.add(down);
                }
                if (left) {
                    Vec3 vertical = new Vec3(0, 1, 0);
                    Vec3 Left = new Vec3(vertical.cross(direction).toVector3f());
                    Left = Left.normalize();
                    movement = movement.add(Left);
                }
                if (right) {
                    Vec3 vertical = new Vec3(0, 1, 0);
                    Vec3 Right = new Vec3(direction.cross(vertical).toVector3f());
                    Right = Right.normalize();
                    movement = movement.add(Right);
                }
                movement = MineraculousMathUtils.projectOnCircle(staffPositionRelativeToThePlayer, movement);
                if (movement.length() > 0.15d) {
                    movement = movement.normalize();
                    movement = movement.scale(0.1d);
                    player.setDeltaMovement(movement);
                    player.hurtMarked = true;
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(movement.toVector3f(), true));
                }
            }
        }
    }

    private static int fromDirectionToInt(Direction direction) {
        if (direction == Direction.NORTH) return 1;
        if (direction == Direction.EAST) return 2;
        if (direction == Direction.SOUTH) return 3;
        if (direction == Direction.WEST) return 4;
        else return 0;
    }

    private static Direction fromDelta(double x, double z) {
        if (z <= 0 && Math.abs(z) > Math.abs(x)) return Direction.NORTH;
        if (x > 0 && Math.abs(x) > Math.abs(z)) return Direction.EAST;
        if (z > 0 && Math.abs(z) > Math.abs(x)) return Direction.SOUTH;
        if (x <= 0 && Math.abs(x) > Math.abs(z)) return Direction.WEST;
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!stack.has(MineraculousDataComponents.ACTIVE))
            return InteractionResultHolder.fail(stack);
        if (stack.has(MineraculousDataComponents.CAT_STAFF_ABILITY)) {
            Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get());
            if (ability == Ability.BLOCK || ability == Ability.THROW || ability == Ability.PERCH)
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
        if (entityLiving instanceof Player player) {
            if (ability == Ability.THROW) {
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
            } else if (ability == Ability.PERCH) {
                PerchCatStaffData perchCatStaffData = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF);
                float groundRY = perchCatStaffData.yGroundLevel();
                float length = perchCatStaffData.length();
                boolean catStaffPerchPerching = perchCatStaffData.startEdge();
                int t = perchCatStaffData.tick();
                boolean nRender = perchCatStaffData.canRender();
                Vector3f initPos = perchCatStaffData.initPos();
                float yBeforeFalling = perchCatStaffData.yBeforeFalling();
                if (groundRY == length && t > 30) {
                    if (!level.isClientSide) {
                        Vector3f lookAngle = new Vector3f((float) player.getLookAngle().x, 0f, (float) player.getLookAngle().z);
                        PerchCatStaffData newPerchData = new PerchCatStaffData(length, groundRY, catStaffPerchPerching, t, nRender, initPos, true, yBeforeFalling, lookAngle);
                        player.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, newPerchData);
                        newPerchData.save(player, true);
                    }
                }
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
