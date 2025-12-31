package dev.thomasglasser.mineraculous.api.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a curio is being rendered with the {@link dev.thomasglasser.mineraculous.api.client.renderer.item.curio.ContextDependentCurioRenderer}.
 * See the two subclasses for listening for before and after rendering,
 * or the {@link DetermineContext} subclass for determining the curio's display context.
 *
 * @see Pre
 * @see Post
 * @see DetermineContext
 * @see dev.thomasglasser.mineraculous.api.client.renderer.item.curio.ContextDependentCurioRenderer
 *
 * @param <T> The entity rendering the curios
 * @param <M> The model of the entity rendering the curios
 */
public abstract class ContextDependentCurioRenderEvent<T extends LivingEntity, M extends EntityModel<T>> extends LivingEvent {
    private final ItemStack stack;
    private final String slot;
    private final PoseStack poseStack;
    private final RenderLayerParent<T, M> renderLayerParent;
    private final MultiBufferSource renderTypeBuffer;
    private final int light;
    private final float limbSwing;
    private final float limbSwingAmount;
    private final float partialTicks;
    private final float ageInTicks;
    private final float netHeadYaw;
    private final float headPitch;

    @ApiStatus.Internal
    public ContextDependentCurioRenderEvent(LivingEntity entity, ItemStack stack, String slot, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        super(entity);
        this.stack = stack;
        this.slot = slot;
        this.poseStack = poseStack;
        this.renderLayerParent = renderLayerParent;
        this.renderTypeBuffer = renderTypeBuffer;
        this.light = light;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.partialTicks = partialTicks;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
    }

    /**
     * Returns the stack of the curio being rendered.
     * 
     * @return The stack of the curio being rendered
     */
    public ItemStack getStack() {
        return stack;
    }

    /**
     * Returns the slot of the curio being rendered.
     * 
     * @return The slot of the curio being rendered
     */
    public String getSlot() {
        return slot;
    }

    /**
     * Returns the pose stack used for rendering.
     * 
     * @return The pose stack used for rendering
     */
    public PoseStack getPoseStack() {
        return poseStack;
    }

    /**
     * Returns the renderer used for rendering.
     * 
     * @return The renderer used for rendering
     */
    public RenderLayerParent<T, M> getRenderLayerParent() {
        return renderLayerParent;
    }

    /**
     * Returns the buffer used for rendering.
     * 
     * @return The buffer used for rendering
     */
    public MultiBufferSource getRenderTypeBuffer() {
        return renderTypeBuffer;
    }

    /**
     * Returns the packed light value used for rendering.
     * 
     * @return The packed light value used for rendering
     */
    public int getLight() {
        return light;
    }

    /**
     * Returns the limb swing value of the entity rendering the curios.
     * 
     * @return The limb swing value of the entity rendering the curios
     */
    public float getLimbSwing() {
        return limbSwing;
    }

    /**
     * Returns the limb swing amount of the entity rendering the curios.
     * 
     * @return The limb swing amount of the entity rendering the curios
     */
    public float getLimbSwingAmount() {
        return limbSwingAmount;
    }

    /**
     * Returns the partial tick of the current render cycle.
     * 
     * @return The partial tick of the current render cycle
     */
    public float getPartialTicks() {
        return partialTicks;
    }

    /**
     * Returns the age in ticks of the entity rendering the curios.
     * 
     * @return The age in ticks of the entity rendering the curios
     */
    public float getAgeInTicks() {
        return ageInTicks;
    }

    /**
     * Returns the net head yaw of the entity rendering the curios.
     * 
     * @return The net head yaw of the entity rendering the curios
     */
    public float getNetHeadYaw() {
        return netHeadYaw;
    }

    /**
     * Returns the head pitch of the entity rendering the curios.
     * 
     * @return The head pitch of the entity rendering the curios
     */
    public float getHeadPitch() {
        return headPitch;
    }

    /**
     * Fired <b>before</b> the curio is rendered.
     * Determines the curio's display context.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @param <T> The type of entity being rendered
     * @param <M> The model of the entity being rendered
     */
    public static class DetermineContext<T extends LivingEntity, M extends EntityModel<T>> extends ContextDependentCurioRenderEvent<T, M> {
        private final HumanoidModel<?> model;

        private ItemDisplayContext context = MineraculousItemDisplayContexts.CURIOS_BODY.getValue();
        private ModelPart modelPart;

        @ApiStatus.Internal
        public DetermineContext(LivingEntity entity, ItemStack stack, String slot, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, HumanoidModel<?> model) {
            super(entity, stack, slot, poseStack, renderLayerParent, renderTypeBuffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            this.model = model;
            this.modelPart = model.body;
        }

        /**
         * Returns the model of the entity being rendered.
         * 
         * @return The model of the entity being rendered
         */
        public HumanoidModel<?> getModel() {
            return model;
        }

        /**
         * Returns the display context of the curio being rendered.
         * 
         * @return The display context of the curio being rendered
         */
        public ItemDisplayContext getDisplayContext() {
            return context;
        }

        /**
         * Returns the model part of the curio being rendered.
         * 
         * @return The model part of the curio being rendered
         */
        public ModelPart getModelPart() {
            return modelPart;
        }

        /**
         * Sets the display context of the curio being rendered.
         * 
         * @param context The new display context of the curio being rendered
         */
        public void setDisplayContext(ItemDisplayContext context) {
            this.context = context;
            this.modelPart = getDefaultPart();
        }

        /**
         * Sets the display context and model part of the curio being rendered.
         * 
         * @param context   The new display context of the curio being rendered
         * @param modelPart The new model part of the curio being rendered.
         */
        public void setDisplayContext(ItemDisplayContext context, ModelPart modelPart) {
            this.context = context;
            this.modelPart = modelPart;
        }

        /**
         * Returns the default model part for the given display context.
         * 
         * @return The default model part for the given display context
         */
        public ModelPart getDefaultPart() {
            if (context == MineraculousItemDisplayContexts.CURIOS_HEAD.getValue() || context == MineraculousItemDisplayContexts.CURIOS_LEFT_EARRING.getValue()) {
                return model.head;
            } else if (context == MineraculousItemDisplayContexts.CURIOS_RIGHT_ARM.getValue()) {
                return model.rightArm;
            } else if (context == MineraculousItemDisplayContexts.CURIOS_LEFT_ARM.getValue()) {
                return model.leftArm;
            } else if (context == MineraculousItemDisplayContexts.CURIOS_RIGHT_LEG.getValue()) {
                return model.rightLeg;
            } else if (context == MineraculousItemDisplayContexts.CURIOS_LEFT_LEG.getValue()) {
                return model.leftLeg;
            }
            return model.body;
        }
    }

    /**
     * Fired <b>before</b> the curio is rendered.
     * This can be used for rendering additional effects or suppressing rendering.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.
     * If this event is cancelled, then the curio will not be rendered and the corresponding
     * {@link Post} will not be fired.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @param <T> The type of entity being rendered
     * @param <M> The model of the entity being rendered
     */
    public static class Pre<T extends LivingEntity, M extends EntityModel<T>> extends ContextDependentCurioRenderEvent<T, M> implements ICancellableEvent {
        private final ItemDisplayContext context;

        @ApiStatus.Internal
        public Pre(LivingEntity entity, ItemStack stack, String slot, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, ItemDisplayContext context) {
            super(entity, stack, slot, poseStack, renderLayerParent, renderTypeBuffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            this.context = context;
        }

        /**
         * Returns the display context of the curio being rendered.
         * 
         * @return The display context of the curio being rendered
         */
        public ItemDisplayContext getDisplayContext() {
            return context;
        }
    }

    /**
     * Fired <b>after</b> the entity is rendered, if the corresponding {@link Pre} is not cancelled.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
     *
     * @param <T> The type of entity being rendered
     * @param <M> The model of the entity being rendered
     */
    public static class Post<T extends LivingEntity, M extends EntityModel<T>> extends ContextDependentCurioRenderEvent<T, M> {
        private final ItemDisplayContext context;
        private final ItemInHandRenderer itemInHandRenderer;

        @ApiStatus.Internal
        public Post(LivingEntity entity, ItemStack stack, String slot, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, ItemDisplayContext context, ItemInHandRenderer itemInHandRenderer) {
            super(entity, stack, slot, poseStack, renderLayerParent, renderTypeBuffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            this.context = context;
            this.itemInHandRenderer = itemInHandRenderer;
        }

        /**
         * Returns the display context of the curio being rendered.
         * 
         * @return The display context of the curio being rendered
         */
        public ItemDisplayContext getDisplayContext() {
            return context;
        }

        /**
         * Returns the item in hand renderer used for rendering the curio.
         * 
         * @return The item in hand renderer used for rendering the curio.
         */
        public ItemInHandRenderer getItemInHandRenderer() {
            return itemInHandRenderer;
        }
    }
}
