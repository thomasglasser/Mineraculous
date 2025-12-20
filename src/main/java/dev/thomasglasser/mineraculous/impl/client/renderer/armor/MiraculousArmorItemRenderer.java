package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.LookRenderer;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.client.model.LookGeoModel;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.look.Look;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MiraculousArmorItemRenderer<T extends Item & GeoItem> extends GeoArmorRenderer<T> implements LookRenderer {
    private final GeoModel<T> model;

    private @Nullable Look look = null;

    public MiraculousArmorItemRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
        this.model = new LookGeoModel<>(this);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return model;
    }

    @Override
    public ResourceLocation getDefaultLookId() {
        return MiraculousItemRenderer.getDefaultLookId(getCurrentStack());
    }

    @Override
    public Holder<LookContext> getContext() {
        return LookContexts.MIRACULOUS_SUIT;
    }

    @Override
    public @Nullable Look getLook() {
        return look;
    }

    @Override
    public void prepForRender(Entity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel, MultiBufferSource bufferSource, float partialTick, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
        super.prepForRender(entity, stack, slot, baseModel, bufferSource, partialTick, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        if (entity instanceof Player player) {
            look = LookManager.getOrFetchLook(player, MiraculousItemRenderer.getMiraculousOrDefault(stack), getContext().getKey());
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        ResourceLocation base = super.getTextureLocation(animatable);
        ItemStack stack = getCurrentStack();
        return MiraculousItemRenderer.getMiraculousOrDefault(stack).value().transformationFrames().map(frames -> {
            MiraculousData.TransformationState state = stack.get(MineraculousDataComponents.TRANSFORMATION_STATE);
            if (state != null) {
                int remaining = state.remainingFrames();
                int frame = state.transforming() ? frames - remaining : remaining;
                if (frame >= 0) {
                    ResourceLocation texture = getAssetOrDefault(LookAssetTypes.TRANSFORMATION_TEXTURES).get(frame);
                    if (MineraculousClientUtils.isValidTexture(texture))
                        return texture;
                }
            }
            return base;
        }).orElse(base);
    }
}
