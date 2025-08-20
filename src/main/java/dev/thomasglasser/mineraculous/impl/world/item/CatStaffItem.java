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
import dev.thomasglasser.mineraculous.api.world.item.component.ActiveSettings;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.network.ServerboundEquipToolPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetDeltaMovementPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelCatStaffData;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.client.renderer.item.GlowingDefaultedGeoItemRenderer;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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

public class CatStaffItem extends SwordItem implements ModeledItem, GeoItem, ProjectileItem, ICurioItem, RadialMenuProvider<CatStaffItem.Ability> {
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String CONTROLLER_EXTEND = "extend_controller";
    public static final String ANIMATION_EXTEND = "extend";
    public static final String ANIMATION_RETRACT = "retract";
    public static float PERCH_STAFF_DISTANCE = 7f / 16f;

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
                if (stack.has(MineraculousDataComponents.BLOCKING))
                    return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
            }
            return PlayState.STOP;
        }));
        controllers.add(new AnimationController<>(this, CONTROLLER_EXTEND, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (stack != null) {
                if (!stack.getOrDefault(MineraculousDataComponents.ACTIVE, false) && !state.isCurrentAnimation(RETRACT))
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
                if (bewlr == null) bewlr = new GlowingDefaultedGeoItemRenderer<>(MineraculousItems.CAT_STAFF.getId());
                return bewlr;
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    //TODO CLEAN THIS
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof Player player)) {
            super.inventoryTick(stack, level, entity, slotId, isSelected);
            return;
        }

        boolean isActive = stack.getOrDefault(MineraculousDataComponents.ACTIVE, false);
        if (!isActive) {
            super.inventoryTick(stack, level, entity, slotId, isSelected);
            return;
        }

        boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY);

        if (ability == Ability.PERCH && inHand) {
            if (level.isClientSide()) {
                PerchCatStaffData perchData = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF);
                boolean isFalling = perchData.isFalling();
                float length = perchData.length();
                float groundRYClient = perchData.yGroundLevel(); //relative to client
                int perchTickClient = perchData.tick();
                if (!isFalling) {
                    float d = 0;
                    boolean k = false;
                    boolean shouldNotFall = (groundRYClient == length);
                    if (MineraculousKeyMappings.WEAPON_DOWN_ARROW.isDown()) {
                        d -= 0.3f;
                        k = true;
                    }
                    if (MineraculousKeyMappings.WEAPON_UP_ARROW.isDown() && Math.abs(groundRYClient) < MineraculousServerConfig.get().maxCatStaffLength.get()) {
                        d += 0.3f;
                        k = true;
                    }
                    if (perchTickClient > 30) {
                        if (k) {
                            Vec3 vec3 = new Vec3(0, d, 0);
                            player.setDeltaMovement(vec3);
                            player.hurtMarked = true;
                            TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(vec3, true));
                        } else if (shouldNotFall) {
                            player.setDeltaMovement(Vec3.ZERO);
                            player.hurtMarked = true;
                            TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(Vec3.ZERO, true));
                        }
                    } else {
                        Vec3 vec3 = new Vec3(0, player.getDeltaMovement().y, 0);
                        player.setDeltaMovement(vec3);
                    }
                }
                constrainPerchMovement(player, stack);
            } else {
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
                    while (level.getBlockState(new BlockPos(entity.getBlockX(), y, entity.getBlockZ())).isEmpty() && Math.abs(entity.getBlockY() - y) <= MineraculousServerConfig.get().maxCatStaffLength.get()) {
                        y--;
                    }
                    y++;
                    catStaffPerchGroundRY = (float) y - (float) entity.getY();
                    yBeforeFalling = (float) player.getY();
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
                }
            }
        } else {
            player.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, new PerchCatStaffData());
            PerchCatStaffData.remove(player, true);
        }

        if (ability == Ability.TRAVEL && inHand) {
            if (player.getCooldowns().isOnCooldown(stack.getItem()))
                entity.resetFallDistance();
            TravelCatStaffData travelCatStaffData = player.getData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF);
            if (travelCatStaffData.traveling()) {
                float length = travelCatStaffData.length();
                boolean didLaunch = travelCatStaffData.launch();
                BlockPos targetPos = travelCatStaffData.blockPos();
                float targetDistance = new Vector3f((float) (player.getX() - targetPos.getX()),
                        (float) (player.getY() - targetPos.getY()),
                        (float) (player.getZ() - targetPos.getZ())).length();
                if (length < targetDistance && length <= MineraculousServerConfig.get().maxCatStaffLength.get()) length += 8;
                if (length > targetDistance) length = targetDistance;
                if (length == targetDistance && !didLaunch) {
                    player.setDeltaMovement(new Vec3(travelCatStaffData.initialLookingAngle()).normalize().scale(4));
                    player.hurtMarked = true;
                    player.getCooldowns().addCooldown(stack.getItem(), 40);
                    didLaunch = true;
                }
                if (didLaunch && player.getDeltaMovement().y < 0.5) {
                    player.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, new TravelCatStaffData());
                    TravelCatStaffData.remove(player, true);
                } else {
                    //SAVE DATA
                    TravelCatStaffData newTravelData = new TravelCatStaffData(length, targetPos, true, travelCatStaffData.initialLookingAngle(), travelCatStaffData.y(), travelCatStaffData.initBodAngle(), didLaunch);
                    player.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, newTravelData);
                    newTravelData.save(player, true);
                }
            }
        } else {
            player.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, new TravelCatStaffData());
            TravelCatStaffData.remove(player, true);
        }

        if (stack.has(MineraculousDataComponents.BLOCKING) && entity.getXRot() <= -75 && entity.getDeltaMovement().y <= 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, -0.1, entity.getDeltaMovement().z);
            entity.resetFallDistance();
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand pHand) {
        ItemStack stack = player.getItemInHand(pHand);
        if (!stack.getOrDefault(MineraculousDataComponents.ACTIVE, false))
            return InteractionResultHolder.fail(stack);
        if (stack.has(MineraculousDataComponents.CAT_STAFF_ABILITY)) {
            Ability ability = stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY.get());
            if (ability == Ability.BLOCK || ability == Ability.THROW || ability == Ability.PERCH)
                player.startUsingItem(pHand);
            else if (ability == Ability.TRAVEL) {
                TravelCatStaffData travelCatStaffData = player.getData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF);
                if (!travelCatStaffData.traveling()) {
                    Vec3 lookAngle = player.getLookAngle().normalize();
                    BlockHitResult result = level.clip(new ClipContext(player.getEyePosition(),
                            player.getEyePosition().add(lookAngle.scale(-MineraculousServerConfig.get().maxCatStaffLength.get())),
                            ClipContext.Block.OUTLINE,
                            ClipContext.Fluid.ANY,
                            player));
                    BlockPos hitPos = travelCatStaffData.blockPos();
                    float length = 0;
                    boolean traveling;
                    if (result.getType() == HitResult.Type.BLOCK) {
                        hitPos = result.getBlockPos();
                        traveling = true;
                    } else traveling = false;

                    double initRot = player.getYRot();
                    if (initRot < 0) //simplify:
                        initRot += 360.0f;
                    if (initRot >= 360.0f)
                        initRot -= 360.0f;

                    //SAVE DATA
                    TravelCatStaffData newTravelData = new TravelCatStaffData(length, hitPos, traveling, lookAngle.toVector3f(), (float) player.getY(), (float) initRot, travelCatStaffData.launch());
                    player.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, newTravelData);
                    newTravelData.save(player, true);
                }
            } else if (ability == Ability.PERCH) {
                if (player.getNearestViewDirection() == Direction.UP)
                    player.setDeltaMovement(new Vec3(0, 0.5, 0));
                else if (player.getNearestViewDirection() == Direction.DOWN) {
                    player.setDeltaMovement(new Vec3(0, -0.5, 0));
                    player.resetFallDistance();
                }
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, pHand);
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
                //TommyLibServices.NETWORK.sendToServer(new ServerboundSetCatStaffAbilityPayload(hand, Ability.BLOCK));
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
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(movement, true));
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
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (stack.has(MineraculousDataComponents.BLOCKING) && remainingUseDuration % 10 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.GENERIC_SPIN.get());
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
                        ThrownCatStaff thrown = new ThrownCatStaff(level, entityLiving, stack);
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

                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (stack.has(MineraculousDataComponents.BLOCKING))
            return UseAnim.BLOCK;
        else if (stack.get(MineraculousDataComponents.CAT_STAFF_ABILITY) == Ability.THROW)
            return UseAnim.SPEAR;
        return UseAnim.NONE;
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
        PHONE((stack, player) -> Mineraculous.Dependencies.TOMMYTECH.isLoaded()),
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
