package dev.thomasglasser.mineraculous.api.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.thomasglasser.mineraculous.Mineraculous;
import java.io.IOException;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class MineraculousRenderStateShards {
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_GLINT_TRANSLUCENT_LIGHTMAP_SHADER = new RenderStateShard.ShaderStateShard(MineraculousRenderStateShards::getRendertypeGlintTranslucentLightmapShader);

    public static final RenderStateShard.TexturingStateShard ITEM_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "item_lucky_charm_texturing", () -> setupLuckyCharmTexturing(4, Mth.PI / 12), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ENTITY_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "entity_lucky_charm_texturing", () -> setupLuckyCharmTexturing(0.75f, Mth.PI / 18), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ARMOR_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "armor_lucky_charm_texturing", () -> setupLuckyCharmTexturing(1, 0), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard SHIELD_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "shield_lucky_charm_texturing", () -> setupLuckyCharmTexturing(16, Mth.PI / 18), RenderSystem::resetTextureMatrix);

    @Nullable
    static ShaderInstance rendertypeGlintTranslucentLightmapShader;

    @Nullable
    public static ShaderInstance getRendertypeGlintTranslucentLightmapShader() {
        return rendertypeGlintTranslucentLightmapShader;
    }

    private static void setupLuckyCharmTexturing(float scale, float rotate) {
        RenderSystem.setTextureMatrix(new Matrix4f().rotateZ(rotate).scale(scale));
    }

    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ResourceProvider resourceProvider = event.getResourceProvider();
            event.registerShader(
                    new ShaderInstance(resourceProvider, Mineraculous.modLoc("rendertype_glint_translucent_lightmap"), DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR),
                    instance -> rendertypeGlintTranslucentLightmapShader = instance);
        } catch (IOException e) {
            Mineraculous.LOGGER.error("Failed to register shaders", e);
        }
    }
}
