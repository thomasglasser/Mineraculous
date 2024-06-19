package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.particle.CataclysmParticle;
import dev.thomasglasser.mineraculous.client.renderer.entity.KamikoRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.KwamiRenderer;
import dev.thomasglasser.mineraculous.client.renderer.entity.ThrownCatStaffRenderer;
import dev.thomasglasser.mineraculous.client.renderer.item.curio.MiraculousItemCurioRenderer;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.network.ServerboundRequestMiraculousDataSetSyncPayload;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

import java.util.Map;

public class MineraculousClientEvents
{
	public static void onEntityJoinLevel(EntityJoinLevelEvent event)
	{
		if (event.getLevel().isClientSide)
		{
			TommyLibServices.NETWORK.sendToServer(new ServerboundRequestMiraculousDataSetSyncPayload(event.getEntity().getId()));
		}
	}

	public static void openPowerWheel(Player player)
	{
		if (ClientUtils.getMinecraft().screen == null)
		{
			// TODO: Radial menu with all available powers from all active miraculous
			player.sendSystemMessage(Component.literal("Power Wheel Coming Soon"));
		}
	}

	public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event)
	{
		ResourceManager manager = Minecraft.getInstance().getResourceManager();
		Map<ResourceLocation, Resource> map = manager.listResources("models/item/miraculous", (location -> location.getPath().endsWith(".json")));
		for (ResourceLocation rl : map.keySet())
		{
			ResourceLocation stripped = new ResourceLocation(rl.getNamespace(), rl.getPath().substring("models/".length(), rl.getPath().indexOf(".json")));
			event.register(stripped);
		}
	}

	public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(MineraculousEntityTypes.TIKKI.get(), context -> new KwamiRenderer<>(context, MineraculousEntityTypes.TIKKI.getId()));
		event.registerEntityRenderer(MineraculousEntityTypes.PLAGG.get(), context -> new KwamiRenderer<>(context, MineraculousEntityTypes.PLAGG.getId()));
		event.registerEntityRenderer(MineraculousEntityTypes.KAMIKO.get(), KamikoRenderer::new);
		event.registerEntityRenderer(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), ThrownCatStaffRenderer::new);
	}

	public static void onFMLClientSetup(FMLClientSetupEvent event)
	{
		CuriosRendererRegistry.register(MineraculousItems.CAT_MIRACULOUS.get(), () -> new ICurioRenderer() {
			private final MiraculousItemCurioRenderer renderer = new MiraculousItemCurioRenderer();

			@Override
			public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource renderTypeBuffer, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
			{
				if (renderLayerParent.getModel() instanceof HumanoidModel<?> humanoidModel)
					renderer.render(stack, slotContext.entity(), humanoidModel, matrixStack, renderTypeBuffer, light);
			}
		});
	}

	public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event)
	{
		event.registerSpriteSet(MineraculousParticleTypes.CATACLYSM.get(), CataclysmParticle.Provider::new);
	}

	public static void onRegisterGuiLayers(RegisterGuiLayersEvent event)
	{
		event.registerAboveAll(Mineraculous.modLoc("stealing_progress_bar"), MineraculousClientEvents::renderStealingProgressBar);
	}

	public static void renderStealingProgressBar(GuiGraphics guiGraphics, float partialTick)
	{
		LocalPlayer player = Minecraft.getInstance().player;
		CompoundTag data = TommyLibServices.ENTITY.getPersistentData(player);
		int width = data.getInt(MineraculousEntityEvents.TAG_TAKETICKS);
		if (player != null && width > 0)
		{
			int x = (guiGraphics.guiWidth() - 18) / 2;
			int y = (guiGraphics.guiHeight() + 12) / 2;
			guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 20, y + 5, -16777216);
			guiGraphics.fill(RenderType.guiOverlay(), x, y, (int)(x + (width / 5.0)), y + 5, 0xFFFFFFF | -16777216);
		}
	}
}