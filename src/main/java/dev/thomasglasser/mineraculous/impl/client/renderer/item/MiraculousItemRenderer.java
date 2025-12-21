package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.LookRenderer;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.client.model.LookGeoModel;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.look.Look;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.UUID;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MiraculousItemRenderer<T extends Item & GeoAnimatable> extends GeoItemRenderer<T> implements LookRenderer {
    private final GeoModel<T> model;

    public MiraculousItemRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));

        this.model = new LookGeoModel<>(this);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return model;
    }

    public static Holder<Miraculous> getMiraculousOrDefault(ItemStack stack) {
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        Level level = ClientUtils.getLevel();
        if (miraculous == null && level != null) {
            miraculous = level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getAny().orElse(null);
        }
        if (miraculous == null) {
            throw new IllegalStateException("Tried to render a Miraculous related item without any registered miraculous");
        }
        return miraculous;
    }

    public static ResourceLocation getDefaultLookId(ResourceKey<Miraculous> miraculous) {
        return LookManager.getDefaultLookId(miraculous, MineraculousRegistries.MIRACULOUS);
    }

    public static ResourceLocation getDefaultLookId(ItemStack stack) {
        return getDefaultLookId(getMiraculousOrDefault(stack).getKey());
    }

    public static Holder<LookContext> getContext(@Nullable MiraculousItem.PowerState powerState) {
        return powerState == MiraculousItem.PowerState.HIDDEN ? LookContexts.HIDDEN_MIRACULOUS : LookContexts.POWERED_MIRACULOUS;
    }

    @Override
    public ResourceLocation getDefaultLookId() {
        return getDefaultLookId(getCurrentItemStack());
    }

    @Override
    public Holder<LookContext> getContext() {
        return getContext(getCurrentItemStack().get(MineraculousDataComponents.POWER_STATE));
    }

    @Override
    public @Nullable Look getLook() {
        ItemStack stack = getCurrentItemStack();
        UUID owner = stack.get(MineraculousDataComponents.OWNER);
        Level level = ClientUtils.getLevel();
        if (owner != null && level != null && level.getEntities().get(owner) instanceof Player player) {
            return LookManager.getOrFetchLook(player, player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(getMiraculousOrDefault(stack)).lookData(), getContext().getKey());
        }
        return null;
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender) {
            ItemTransform transform;
            ItemTransforms transforms = getAsset(LookAssetTypes.ITEM_TRANSFORMS);
            if (transforms != null && transforms.hasTransform(renderPerspective)) {
                transform = transforms.getTransform(renderPerspective);
            } else {
                transform = getDefaultAsset(LookAssetTypes.ITEM_TRANSFORMS).getTransform(renderPerspective);
            }
            transform.apply(false, poseStack);
        }
        // Special case for earrings
        model.getBone("left_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_RIGHT_EARRING.getValue()));
        model.getBone("right_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_LEFT_EARRING.getValue()));
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        MiraculousItem.PowerState powerState = getCurrentItemStack().getOrDefault(MineraculousDataComponents.POWER_STATE, MiraculousItem.PowerState.POWERED);
        return switch (powerState) {
            case HIDDEN, POWERED -> getAssetOrDefault(LookAssetTypes.TEXTURE);
            case ACTIVE -> getAssetOrDefault(LookAssetTypes.TEXTURE, LookContexts.ACTIVE_MIRACULOUS);
            default -> getAssetOrDefault(LookAssetTypes.COUNTDOWN_TEXTURES).get(powerState.frame() - 1);
        };
    }
}
