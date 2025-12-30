package dev.thomasglasser.mineraculous.api.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

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

    public ItemStack getStack() {
        return stack;
    }

    public String getSlot() {
        return slot;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public RenderLayerParent<T, M> getRenderLayerParent() {
        return renderLayerParent;
    }

    public MultiBufferSource getRenderTypeBuffer() {
        return renderTypeBuffer;
    }

    public int getLight() {
        return light;
    }

    public float getLimbSwing() {
        return limbSwing;
    }

    public float getLimbSwingAmount() {
        return limbSwingAmount;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public float getAgeInTicks() {
        return ageInTicks;
    }

    public float getNetHeadYaw() {
        return netHeadYaw;
    }

    public float getHeadPitch() {
        return headPitch;
    }

    public static class DetermineContext<T extends LivingEntity, M extends EntityModel<T>> extends ContextDependentCurioRenderEvent<T, M> {
        private ItemDisplayContext context = MineraculousItemDisplayContexts.CURIOS_BODY.getValue();

        public DetermineContext(LivingEntity entity, ItemStack stack, String slot, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            super(entity, stack, slot, poseStack, renderLayerParent, renderTypeBuffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }

        public ItemDisplayContext getDisplayContext() {
            return context;
        }

        public void setDisplayContext(ItemDisplayContext context) {
            this.context = context;
        }
    }

    public static class Pre<T extends LivingEntity, M extends EntityModel<T>> extends ContextDependentCurioRenderEvent<T, M> implements ICancellableEvent {
        private final ItemDisplayContext context;

        public Pre(LivingEntity entity, ItemStack stack, String slot, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, ItemDisplayContext context) {
            super(entity, stack, slot, poseStack, renderLayerParent, renderTypeBuffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            this.context = context;
        }

        public ItemDisplayContext getDisplayContext() {
            return context;
        }
    }

    public static class Post<T extends LivingEntity, M extends EntityModel<T>> extends ContextDependentCurioRenderEvent<T, M> {
        private final ItemDisplayContext context;
        private final ItemInHandRenderer itemInHandRenderer;

        public Post(LivingEntity entity, ItemStack stack, String slot, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, ItemDisplayContext context, ItemInHandRenderer itemInHandRenderer) {
            super(entity, stack, slot, poseStack, renderLayerParent, renderTypeBuffer, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            this.context = context;
            this.itemInHandRenderer = itemInHandRenderer;
        }

        public ItemDisplayContext getDisplayContext() {
            return context;
        }

        public ItemInHandRenderer getItemInHandRenderer() {
            return itemInHandRenderer;
        }
    }
}
