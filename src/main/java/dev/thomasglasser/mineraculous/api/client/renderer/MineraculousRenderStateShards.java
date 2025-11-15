package dev.thomasglasser.mineraculous.api.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import java.io.IOException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class MineraculousRenderStateShards {
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_NO_LIGHTMAP_SHADER = new RenderStateShard.ShaderStateShard(MineraculousRenderStateShards::getRendertypeEntityTranslucentEmissiveNoLightmapShader);
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_GLINT_TRANSLUCENT_LIGHTMAP_SHADER = new RenderStateShard.ShaderStateShard(MineraculousRenderStateShards::getRendertypeGlintTranslucentLightmapShader);

    public static final RenderStateShard.TexturingStateShard ITEM_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "item_lucky_charm_texturing", () -> setupLuckyCharmTexturing(4, Mth.PI / 12), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ENTITY_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "entity_lucky_charm_texturing", () -> setupLuckyCharmTexturing(0.75f, Mth.PI / 18), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ARMOR_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "armor_lucky_charm_texturing", () -> setupLuckyCharmTexturing(1, 0), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard SHIELD_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "shield_lucky_charm_texturing", () -> setupLuckyCharmTexturing(16, Mth.PI / 18), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ITEM_KAMIKOTIZING_TEXTURING = new RenderStateShard.TexturingStateShard(
            "item_kamikotizing_texturing", () -> setupKamikotizingTexturing(4, Mth.PI / 12, 1), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ENTITY_KAMIKOTIZING_TEXTURING = new RenderStateShard.TexturingStateShard(
            "entity_kamikotizing_texturing", () -> setupKamikotizingTexturing(0.75f, Mth.PI / 18, 8), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ARMOR_KAMIKOTIZING_TEXTURING = new RenderStateShard.TexturingStateShard(
            "armor_kamikotizing_texturing", () -> setupKamikotizingTexturing(1, 0, 4), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard SHIELD_KAMIKOTIZING_TEXTURING = new RenderStateShard.TexturingStateShard(
            "shield_kamikotizing_texturing", () -> setupKamikotizingTexturing(16, Mth.PI / 18, 8), RenderSystem::resetTextureMatrix);

    @Nullable
    private static ShaderInstance rendertypeEntityTranslucentEmissiveNoLightmapShader;

    @Nullable
    public static ShaderInstance getRendertypeEntityTranslucentEmissiveNoLightmapShader() {
        return rendertypeEntityTranslucentEmissiveNoLightmapShader;
    }

    @Nullable
    private static ShaderInstance rendertypeGlintTranslucentLightmapShader;

    @Nullable
    public static ShaderInstance getRendertypeGlintTranslucentLightmapShader() {
        return rendertypeGlintTranslucentLightmapShader;
    }

    private static void setupLuckyCharmTexturing(float scale, float rotate) {
        RenderSystem.setTextureMatrix(new Matrix4f().rotateZ(rotate).scale(scale));
    }

    private static void setupKamikotizingTexturing(float scale, float rotate, int speedMultiplier) {
        long i = (long) ((double) Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * speedMultiplier);
        float f = (float) (i % 110000L) / 110000.0F;
        float f1 = (float) (i % 30000L) / 30000.0F;
        Matrix4f matrix4f = new Matrix4f().translation(-f, f1, 0.0F);
        matrix4f.rotateZ(rotate).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }

    @ApiStatus.Internal
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ResourceProvider resourceProvider = event.getResourceProvider();
            event.registerShader(
                    new ShaderInstance(resourceProvider, MineraculousConstants.modLoc("rendertype_entity_translucent_emissive_no_lightmap"), DefaultVertexFormat.NEW_ENTITY),
                    instance -> rendertypeEntityTranslucentEmissiveNoLightmapShader = instance);
            event.registerShader(
                    new ShaderInstance(resourceProvider, MineraculousConstants.modLoc("rendertype_glint_translucent_lightmap"), DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR),
                    instance -> rendertypeGlintTranslucentLightmapShader = instance);
        } catch (IOException e) {
            MineraculousConstants.LOGGER.error("Failed to register shaders", e);
        }
    }
}
