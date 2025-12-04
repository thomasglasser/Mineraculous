package dev.thomasglasser.mineraculous.impl.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.PlayerLikeCapeLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.PlayerLikeDeadmau5EarsLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.PlayerLikeItemInHandLayer;
import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.PlayerLikeParrotOnShoulderLayer;
import dev.thomasglasser.mineraculous.impl.world.entity.PlayerLike;
import dev.thomasglasser.tommylib.impl.GeckoLibUtils;
import java.util.Map;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class PlayerLikeRenderer<T extends LivingEntity & PlayerLike> extends LivingEntityRenderer<T, PlayerModel<T>> {
    private static final Map<PlayerSkin.Model, EntityRendererProvider<?>> PROVIDERS = ImmutableMap.of(
            PlayerSkin.Model.WIDE, context -> new PlayerLikeRenderer(context, false),
            PlayerSkin.Model.SLIM, context -> new PlayerLikeRenderer(context, true));
    private static Map<PlayerSkin.Model, PlayerLikeRenderer<?>> RENDERERS = ImmutableMap.of();

    public PlayerLikeRenderer(EntityRendererProvider.Context context, boolean useSlimModel) {
        super(context, new PlayerModel<>(context.bakeLayer(useSlimModel ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), useSlimModel), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(context.bakeLayer(useSlimModel ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel<>(context.bakeLayer(useSlimModel ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
        this.addLayer(new PlayerLikeItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new ArrowLayer<>(context, this));
        this.addLayer(new PlayerLikeDeadmau5EarsLayer<>(this));
        this.addLayer(new PlayerLikeCapeLayer<>(this));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, context.getModelSet()));
        this.addLayer(new PlayerLikeParrotOnShoulderLayer<>(this, context.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer<>(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this));
    }

    public static void refreshModels(EntityRendererProvider.Context context) {
        ImmutableMap.Builder<PlayerSkin.Model, PlayerLikeRenderer<?>> builder = ImmutableMap.builder();
        PROVIDERS.forEach((model, provider) -> {
            try {
                builder.put(model, (PlayerLikeRenderer<?>) provider.create(context));
            } catch (Exception exception) {
                throw new IllegalArgumentException("Failed to create player-like model for " + model, exception);
            }
        });
        RENDERERS = builder.build();
    }

    public static PlayerLikeRenderer<?> get(PlayerSkin.Model model) {
        return RENDERERS.get(model);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.setModelProperties(entity);
        // TODO: Similar events
        if (true/*!NeoForge.EVENT_BUS.post(new RenderPlayerEvent.Pre(entity, this, partialTicks, poseStack, buffer, packedLight)).isCanceled()*/) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
//            NeoForge.EVENT_BUS.post(new RenderPlayerEvent.Post(entity, this, partialTicks, poseStack, buffer, packedLight));
        }
    }

    @Override
    public Vec3 getRenderOffset(T entity, float partialTicks) {
        return entity.isCrouching() ? new Vec3(0.0F, (double) (entity.getScale() * -2.0F) / (double) 16.0F, 0.0F) : super.getRenderOffset(entity, partialTicks);
    }

    private void setModelProperties(T entity) {
        PlayerModel<T> playermodel = this.getModel();
        if (entity.isSpectator()) {
            playermodel.setAllVisible(false);
            playermodel.head.visible = true;
            playermodel.hat.visible = true;
        } else {
            playermodel.setAllVisible(true);
            boolean isHeadSkintight = GeckoLibUtils.isSkintight(entity.getItemBySlot(EquipmentSlot.HEAD).getItem());
            playermodel.hat.visible = !isHeadSkintight && isModelPartShown(entity, PlayerModelPart.HAT);
            boolean isChestSkintight = GeckoLibUtils.isSkintight(entity.getItemBySlot(EquipmentSlot.CHEST).getItem());
            playermodel.jacket.visible = !isChestSkintight && isModelPartShown(entity, PlayerModelPart.JACKET);
            playermodel.leftSleeve.visible = !isChestSkintight && isModelPartShown(entity, PlayerModelPart.LEFT_SLEEVE);
            playermodel.rightSleeve.visible = !isChestSkintight && isModelPartShown(entity, PlayerModelPart.RIGHT_SLEEVE);
            boolean isLowerBodySkintight = GeckoLibUtils.isSkintight(entity.getItemBySlot(EquipmentSlot.LEGS).getItem()) || GeckoLibUtils.isSkintight(entity.getItemBySlot(EquipmentSlot.FEET).getItem());
            playermodel.leftPants.visible = !isLowerBodySkintight && isModelPartShown(entity, PlayerModelPart.LEFT_PANTS_LEG);
            playermodel.rightPants.visible = !isLowerBodySkintight && isModelPartShown(entity, PlayerModelPart.RIGHT_PANTS_LEG);
            playermodel.crouching = entity.isCrouching();
            HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(entity, InteractionHand.MAIN_HAND);
            HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(entity, InteractionHand.OFF_HAND);
            if (humanoidmodel$armpose.isTwoHanded()) {
                humanoidmodel$armpose1 = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
            }

            if (entity.getMainArm() == HumanoidArm.RIGHT) {
                playermodel.rightArmPose = humanoidmodel$armpose;
                playermodel.leftArmPose = humanoidmodel$armpose1;
            } else {
                playermodel.rightArmPose = humanoidmodel$armpose1;
                playermodel.leftArmPose = humanoidmodel$armpose;
            }
        }
    }

    public static <T extends LivingEntity & PlayerLike> boolean isModelPartShown(T entity, PlayerModelPart part) {
        if (entity.getVisualSource() instanceof AbstractClientPlayer player) {
            return player.isModelPartShown(part);
        }
        return true;
    }

    private static HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand) {
        ItemStack itemstack = entity.getItemInHand(hand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
                UseAnim useanim = itemstack.getUseAnimation();
                if (useanim == UseAnim.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (useanim == UseAnim.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (useanim == UseAnim.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (useanim == UseAnim.CROSSBOW && hand == entity.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (useanim == UseAnim.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }

                if (useanim == UseAnim.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }

                if (useanim == UseAnim.BRUSH) {
                    return HumanoidModel.ArmPose.BRUSH;
                }
            } else if (!entity.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            HumanoidModel.ArmPose forgeArmPose = IClientItemExtensions.of(itemstack).getArmPose(entity, hand, itemstack);
            return forgeArmPose != null ? forgeArmPose : HumanoidModel.ArmPose.ITEM;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        if (entity.getVisualSource() instanceof AbstractClientPlayer player) {
            return player.getSkin().texture();
        }
        return null;
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTickTime) {
        float scale = 0.9375F;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    protected void renderNameTag(T entity, Component displayName, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, float partialTick) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        poseStack.pushPose();
        if (d0 < (double) 100.0F) {
            Scoreboard scoreboard = entity.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (objective != null) {
                ReadOnlyScoreInfo readonlyscoreinfo = scoreboard.getPlayerScoreInfo(entity, objective);
                Component component = ReadOnlyScoreInfo.safeFormatValue(readonlyscoreinfo, objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
                super.renderNameTag(entity, Component.empty().append(component).append(CommonComponents.SPACE).append(objective.getDisplayName()), poseStack, bufferSource, packedLight, partialTick);
                poseStack.translate(0.0F, 0.25875F, 0.0F);
            }
        }

        super.renderNameTag(entity, displayName, poseStack, bufferSource, packedLight, partialTick);
        poseStack.popPose();
    }

    public void renderRightHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, T entity) {
        // TODO: Similar event
        if (true/*!ClientHooks.renderSpecificFirstPersonArm(poseStack, buffer, combinedLight, player, HumanoidArm.RIGHT)*/) {
            this.renderHand(poseStack, buffer, combinedLight, entity, this.model.rightArm, this.model.rightSleeve);
        }
    }

    public void renderLeftHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, T entity) {
        // TODO: Similar event
        if (true/*!ClientHooks.renderSpecificFirstPersonArm(poseStack, buffer, combinedLight, player, HumanoidArm.LEFT)*/) {
            this.renderHand(poseStack, buffer, combinedLight, entity, this.model.leftArm, this.model.leftSleeve);
        }
    }

    private void renderHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, T entity, ModelPart rendererArm, ModelPart rendererArmwear) {
        PlayerModel<T> playermodel = this.getModel();
        this.setModelProperties(entity);
        playermodel.attackTime = 0.0F;
        playermodel.crouching = false;
        playermodel.swimAmount = 0.0F;
        playermodel.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        rendererArm.xRot = 0.0F;
        if (entity.getVisualSource() instanceof AbstractClientPlayer player) {
            ResourceLocation resourcelocation = player.getSkin().texture();
            rendererArm.render(poseStack, buffer.getBuffer(RenderType.entitySolid(resourcelocation)), combinedLight, OverlayTexture.NO_OVERLAY);
            rendererArmwear.xRot = 0.0F;
            rendererArmwear.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(resourcelocation)), combinedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    protected void setupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        float f = entity.getSwimAmount(partialTick);
        float f1 = entity.getViewXRot(partialTick);
        if (entity.isFallFlying()) {
            super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
            float f2 = (float) entity.getFallFlyingTicks() + partialTick;
            float f3 = Mth.clamp(f2 * f2 / 100.0F, 0.0F, 1.0F);
            if (!entity.isAutoSpinAttack()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(f3 * (-90.0F - f1)));
            }

            Vec3 vec3 = entity.getViewVector(partialTick);
            Vec3 vec31 = entity.getDeltaMovement();
            double d0 = vec31.horizontalDistanceSqr();
            double d1 = vec3.horizontalDistanceSqr();
            if (d0 > (double) 0.0F && d1 > (double) 0.0F) {
                double d2 = (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1);
                double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
                poseStack.mulPose(Axis.YP.rotation((float) (Math.signum(d3) * Math.acos(d2))));
            }
        } else if (f > 0.0F) {
            super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
            float f4 = !entity.isInWater() && !entity.isInFluidType((fluidType, height) -> entity.canSwimInFluidType(fluidType)) ? -90.0F : -90.0F - entity.getXRot();
            float f5 = Mth.lerp(f, 0.0F, f4);
            poseStack.mulPose(Axis.XP.rotationDegrees(f5));
            if (entity.isVisuallySwimming()) {
                poseStack.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        }
    }
}
