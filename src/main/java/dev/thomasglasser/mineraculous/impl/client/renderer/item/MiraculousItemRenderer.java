package dev.thomasglasser.mineraculous.impl.client.renderer.item;

import com.google.common.collect.ImmutableList;
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
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.look.Look;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
    private static final Set<MiraculousItemRenderer<?>> INSTANCES = new ReferenceOpenHashSet<>();
    private static final Map<ResourceKey<Miraculous>, ModelResourceLocation> MODEL_LOCATIONS = new Reference2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, EnumMap<MiraculousItem.PowerState, ResourceLocation>> POWERED_FRAME_TEXTURES = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> POWERED_TEXTURES = new Object2ReferenceOpenHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> HIDDEN_TEXTURES = new Object2ReferenceOpenHashMap<>();

    private final Map<Holder<Miraculous>, GeoModel<?>> models = new Object2ReferenceOpenHashMap<>();

    public MiraculousItemRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));

        INSTANCES.add(this);
    }

    public static void clearAssets() {
        MODEL_LOCATIONS.clear();
        POWERED_FRAME_TEXTURES.clear();
        POWERED_TEXTURES.clear();
        HIDDEN_TEXTURES.clear();

        INSTANCES.forEach(renderer -> renderer.models.clear());
        INSTANCES.clear();
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

    public static Holder<LookContext> getContext(@Nullable MiraculousItem.PowerState powerState) {
        return powerState == MiraculousItem.PowerState.HIDDEN ? LookContexts.HIDDEN_MIRACULOUS : LookContexts.POWERED_MIRACULOUS;
    }

    @Override
    public Holder<LookContext> getCurrentLookContext() {
        return getContext(getCurrentItemStack().get(MineraculousDataComponents.POWER_STATE));
    }

    @Override
    public @Nullable Look getCurrentLook() {
        ItemStack stack = getCurrentItemStack();
        Integer carrier = stack.get(MineraculousDataComponents.CARRIER);
        Level level = ClientUtils.getLevel();
        if (carrier != null && level != null && level.getEntities().get(carrier) instanceof Player player) {
            return LookManager.getOrFetchLook(player, getMiraculousOrDefault(stack), getCurrentLookContext().getKey());
        }
        return null;
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender) {
            ItemStack stack = getCurrentItemStack();
            ItemTransforms transforms = getAsset(LookAssetTypes.ITEM_TRANSFORMS);
            if (transforms != null && transforms.hasTransform(renderPerspective)) {
                transforms.getTransform(renderPerspective).apply(false, poseStack);
            } else {
                BakedModel miraculousModel = Minecraft.getInstance().getModelManager().getModel(MODEL_LOCATIONS.computeIfAbsent(getMiraculousOrDefault(stack).getKey(), key -> ModelResourceLocation.standalone(key.location().withPrefix("item/miraculous/"))));
                if (miraculousModel != Minecraft.getInstance().getModelManager().getMissingModel()) {
                    miraculousModel.applyTransform(renderPerspective, poseStack, false);
                }
            }
        }
        // Special case for earrings
        model.getBone("left_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_RIGHT_EARRING.getValue()));
        model.getBone("right_earring").ifPresent(bone -> bone.setHidden(renderPerspective == MineraculousItemDisplayContexts.CURIOS_LEFT_EARRING.getValue()));
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        ResourceLocation base = super.getTextureLocation(animatable);
        MiraculousItem.PowerState powerState = getCurrentItemStack().getOrDefault(MineraculousDataComponents.POWER_STATE, MiraculousItem.PowerState.POWERED);
        return switch (powerState) {
            case HIDDEN -> getAssetOrDefault(LookAssetTypes.TEXTURE, () -> HIDDEN_TEXTURES.computeIfAbsent(base, loc -> loc.withPath(path -> path.replace("active", "hidden"))));
            case ACTIVE -> getAssetOrDefault(LookAssetTypes.TEXTURE, LookContexts.ACTIVE_MIRACULOUS, () -> base);
            case POWERED -> getAssetOrDefault(LookAssetTypes.TEXTURE, () -> POWERED_TEXTURES.computeIfAbsent(base, loc -> loc.withPath(path -> path.replace("active", "powered"))));
            default -> {
                ImmutableList<ResourceLocation> countdownTextures = getAsset(LookAssetTypes.COUNTDOWN_TEXTURES);
                if (countdownTextures != null) {
                    yield countdownTextures.get(powerState.frame() - 1);
                }
                yield POWERED_FRAME_TEXTURES.computeIfAbsent(base, loc -> new EnumMap<>(MiraculousItem.PowerState.class)).computeIfAbsent(powerState, state -> base.withPath(path -> path.replace("active", "powered_" + state.frame())));
            }
        };
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return (GeoModel<T>) models.computeIfAbsent(getMiraculousOrDefault(getCurrentItemStack()), this::createGeoModel);
    }

    private GeoModel<T> createGeoModel(Holder<Miraculous> miraculous) {
        ResourceLocation miraculousId = miraculous.getKey().location();
        return new LookGeoModel<>(this, miraculousId.withPrefix("miraculous/"), miraculousId.withPath(id -> "textures/item/miraculous/" + id + "/active.png"));
    }
}
