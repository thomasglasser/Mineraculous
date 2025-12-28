package dev.thomasglasser.mineraculous.impl.client.renderer.armor;

import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.util.renderer.LookRenderer;
import dev.thomasglasser.mineraculous.api.client.model.LookGeoModel;
import dev.thomasglasser.mineraculous.api.client.renderer.layer.ConditionalAutoGlowingGeoLayer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.UUID;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class KamikotizationArmorItemRenderer<T extends Item & GeoItem> extends GeoArmorRenderer<T> implements LookRenderer {
    private final GeoModel<T> model;

    private @Nullable Look<?> look = null;

    public KamikotizationArmorItemRenderer() {
        super((GeoModel<T>) null);
        addRenderLayer(new ConditionalAutoGlowingGeoLayer<>(this));
        this.model = new LookGeoModel<>(this);
    }

    public static Holder<Kamikotization> getKamikotizationOrDefault(ItemStack stack) {
        Holder<Kamikotization> kamikotization = stack.get(MineraculousDataComponents.KAMIKOTIZATION);
        Level level = ClientUtils.getLevel();
        if (kamikotization == null && level != null) {
            kamikotization = level.registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION).getAny().orElse(null);
        }
        if (kamikotization == null) {
            throw new IllegalStateException("Tried to render a Kamikotization related item without any registered kamikotizations");
        }
        return kamikotization;
    }

    public static ResourceLocation getDefaultLookId(ItemStack stack) {
        return LookUtils.getDefaultLookId(getKamikotizationOrDefault(stack).getKey());
    }

    public static @Nullable Look getLook(ItemStack stack, Holder<LookContext> context) {
        UUID owner = stack.get(MineraculousDataComponents.OWNER);
        Level level = ClientUtils.getLevel();
        if (owner != null && level != null && level.getEntities().get(owner) instanceof Player player) {
            return player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::lookData).map(lookData -> LookManager.getLook(lookData, context.getKey())).orElse(null);
        }
        return null;
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return model;
    }

    @Override
    public ResourceLocation getDefaultLookId() {
        return getDefaultLookId(getCurrentStack());
    }

    @Override
    public Holder<LookContext> getContext() {
        return LookContexts.KAMIKOTIZATION_SUIT;
    }

    @Override
    public @Nullable Look getLook() {
        return look;
    }

    @Override
    public void prepForRender(Entity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel, MultiBufferSource bufferSource, float partialTick, float limbSwing, float limbSwingAmount, float netHeadYaw, float headPitch) {
        super.prepForRender(entity, stack, slot, baseModel, bufferSource, partialTick, limbSwing, limbSwingAmount, netHeadYaw, headPitch);
        look = getLook(getCurrentStack(), getContext());
    }
}
