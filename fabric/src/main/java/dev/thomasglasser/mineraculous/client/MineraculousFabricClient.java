package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import dev.thomasglasser.mineraculous.client.renderer.entity.PlaggRenderer;
import dev.thomasglasser.mineraculous.client.renderer.item.curio.MiraculousItemCurioRenderer;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRenderEvents;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MineraculousFabricClient implements ClientModInitializer
{

	@Override
	public void onInitializeClient()
	{
		PreparableModelLoadingPlugin.register(((resourceManager, executor) ->
		{
			Map<ResourceLocation, Resource> map = resourceManager.listResources("models/item/miraculous", (location -> location.getPath().endsWith(".json")));
			List<ModelResourceLocation> rls = new ArrayList<>();
			for (ResourceLocation rl : map.keySet())
			{
				ResourceLocation stripped = new ResourceLocation(rl.getNamespace(), rl.getPath().substring("models/item/".length(), rl.getPath().indexOf(".json")));
				rls.add(new ModelResourceLocation(stripped, "inventory"));
			}
			return CompletableFuture.supplyAsync(() -> rls, executor);
		}), (data, pluginContext) ->
				pluginContext.addModels(data));

		registerRenderers();

		registerTrinketRenderers();

		ClientEntityEvents.ENTITY_LOAD.register(((trackedEntity, player) ->
				MineraculousClientEvents.onEntityJoinLevel(trackedEntity)));
		LivingEntityFeatureRenderEvents.ALLOW_CAPE_RENDER.register(player -> !Services.DATA.getMiraculousDataSet(player).getTransformed().isEmpty());
	}

	private void registerRenderers()
	{
		EntityRendererRegistry.register(MineraculousEntityTypes.PLAGG.get(), PlaggRenderer::new);
	}

	private void registerTrinketRenderers()
	{
		TrinketRendererRegistry.registerRenderer(MineraculousItems.CAT_MIRACULOUS.get(), new TrinketRenderer() {
			private final MiraculousItemCurioRenderer renderer = new MiraculousItemCurioRenderer();

			@Override
			public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> contextModel, PoseStack matrices, MultiBufferSource vertexConsumers, int light, LivingEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch)
			{
				if (contextModel instanceof HumanoidModel<? extends LivingEntity> humanoidModel)
					renderer.render(stack, entity, humanoidModel, matrices, vertexConsumers, light);
			}
		});
	}
}
