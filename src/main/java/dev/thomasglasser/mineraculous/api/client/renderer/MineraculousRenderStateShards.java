package dev.thomasglasser.mineraculous.api.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import java.io.IOException;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class MineraculousRenderStateShards {
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_GLINT_TRANSLUCENT_LIGHTMAP_SHADER = new RenderStateShard.ShaderStateShard(MineraculousRenderStateShards::getRendertypeGlintTranslucentLightmapShader);

    public static final RenderStateShard.TexturingStateShard ITEM_SHADER_TEXTURING = new RenderStateShard.TexturingStateShard(
            "item_shader_texturing", () -> setupItemShaderTexturing(4, Mth.PI / 12), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ENTITY_SHADER_TEXTURING = new RenderStateShard.TexturingStateShard(
            "entity_shader_texturing", () -> setupItemShaderTexturing(0.75f, Mth.PI / 18), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ARMOR_SHADER_TEXTURING = new RenderStateShard.TexturingStateShard(
            "armor_shader_texturing", () -> setupItemShaderTexturing(1, 0), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard SHIELD_SHADER_TEXTURING = new RenderStateShard.TexturingStateShard(
            "shield_shader_texturing", () -> setupItemShaderTexturing(16, Mth.PI / 18), RenderSystem::resetTextureMatrix);

    @Nullable
    private static ShaderInstance rendertypeGlintTranslucentLightmapShader;

    @Nullable
    public static ShaderInstance getRendertypeGlintTranslucentLightmapShader() {
        return rendertypeGlintTranslucentLightmapShader;
    }

    private static void setupItemShaderTexturing(float scale, float rotate) {
        RenderSystem.setTextureMatrix(new Matrix4f().rotateZ(rotate).scale(scale));
    }

    @ApiStatus.Internal
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ResourceProvider resourceProvider = event.getResourceProvider();
            event.registerShader(
                    new ShaderInstance(resourceProvider, MineraculousConstants.modLoc("rendertype_glint_translucent_lightmap"), DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR),
                    instance -> rendertypeGlintTranslucentLightmapShader = instance);
        } catch (IOException e) {
            MineraculousConstants.LOGGER.error("Failed to register shaders", e);
        }
    }
}
