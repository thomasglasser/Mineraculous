package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class MineraculousRenderStateShards {
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_GLINT_TRANSLUCENT_LIGHTMAP_SHADER = new RenderStateShard.ShaderStateShard(MineraculousRenderStateShards::getRendertypeGlintTranslucentLightmapShader);

    public static final RenderStateShard.TexturingStateShard LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "lucky_charm_texturing", () -> setupLuckyCharmTexturing(4.0F, (float) (Math.PI / 12)), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ENTITY_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "entity_lucky_charm_texturing", () -> setupLuckyCharmTexturing(0.75F, (float) (Math.PI / 18)), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard ARMOR_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "armor_lucky_charm_texturing", () -> setupLuckyCharmTexturing(1.0f, 0f), RenderSystem::resetTextureMatrix);

    public static final RenderStateShard.TexturingStateShard SHIELD_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "shield_lucky_charm_texturing", () -> setupLuckyCharmTexturing(16.0F, (float) (Math.PI / 18)), RenderSystem::resetTextureMatrix);

    @Nullable
    static ShaderInstance rendertypeGlintTranslucentLightmapShader;

    @Nullable
    public static ShaderInstance getRendertypeGlintTranslucentLightmapShader() {
        return rendertypeGlintTranslucentLightmapShader;
    }

    private static void setupLuckyCharmTexturing(float scale, float rotate) {
        float speed = 0;
        long i = (long) ((double) Util.getMillis() * speed * 8.0);
        float f = (float) (i % 110000L) / 110000.0F;
        float f1 = (float) (i % 30000L) / 30000.0F;
        Matrix4f matrix4f = new Matrix4f().translation(-f, f1, 0.0F);
        matrix4f.rotateZ(rotate).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }
}
