package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class MineraculousRenderTypes {
    private static final ResourceLocation LUCKY_CHARM_LOCATION = Mineraculous.modLoc("textures/misc/lucky_charm.png");
    private static final ResourceLocation LUCKY_CHARM_ENTITY_LOCATION = Mineraculous.modLoc("textures/misc/lucky_charm_entity.png");

    public static final RenderStateShard.TexturingStateShard LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "lucky_charm_texturing", () -> setupLuckyCharmTexturing(4.0F, (float) (Math.PI / 12)), () -> RenderSystem.resetTextureMatrix());

    public static final RenderStateShard.TexturingStateShard ENTITY_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "lucky_charm_texturing", () -> setupLuckyCharmTexturing(0.75F, (float) (Math.PI / 18)), () -> RenderSystem.resetTextureMatrix());

    public static final RenderStateShard.TexturingStateShard ARMOR_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "entity_lucky_charm_texturing", () -> setupLuckyCharmTexturing(1.0f, 0f), () -> RenderSystem.resetTextureMatrix());

    public static final RenderStateShard.TexturingStateShard SHIELD_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "lucky_charm_texturing", () -> setupLuckyCharmTexturing(8.0F, (float) (Math.PI / 18)), () -> RenderSystem.resetTextureMatrix());

    public static void setupLuckyCharmTexturing(float scale, float rotate) {
        float speed = 0;
        long i = (long) ((double) Util.getMillis() * speed * 8.0);
        float f = (float) (i % 110000L) / 110000.0F;
        float f1 = (float) (i % 30000L) / 30000.0F;
        Matrix4f matrix4f = new Matrix4f().translation(-f, f1, 0.0F);
        matrix4f.rotateZ(rotate).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }

    // RenderStateShard test = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeGlintShader);

    private static final RenderType LUCKY_CHARM = RenderType.create(
            "mineraculous:lucky_charm",
            DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(LUCKY_CHARM_LOCATION, false, true))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTexturingState(LUCKY_CHARM_TEXTURING)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .createCompositeState(false));

    public static final RenderType ARMOR_LUCKY_CHARM = RenderType.create(
            "mineraculous:armor_lucky_charm",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(LUCKY_CHARM_ENTITY_LOCATION, false, true))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTexturingState(ARMOR_LUCKY_CHARM_TEXTURING)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .createCompositeState(false));

    private static final RenderType ENTITY_LUCKY_CHARM = RenderType.create(
            "mineraculous:entity_lucky_charm",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTextureState(new RenderStateShard.TextureStateShard(LUCKY_CHARM_ENTITY_LOCATION, false, true))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTexturingState(ENTITY_LUCKY_CHARM_TEXTURING)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .createCompositeState(false));

    private static final RenderType SHIELD_LUCKY_CHARM = RenderType.create(
            "mineraculous:shield_lucky_charm",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTextureState(new RenderStateShard.TextureStateShard(LUCKY_CHARM_ENTITY_LOCATION, false, true))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTexturingState(SHIELD_LUCKY_CHARM_TEXTURING)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .createCompositeState(false));

    public static RenderType luckyCharm() {
        return LUCKY_CHARM;
    }

    public static RenderType armorLuckyCharm() {
        return ARMOR_LUCKY_CHARM;
    }

    public static RenderType entityLuckyCharm() {
        return ENTITY_LUCKY_CHARM;
    }

    public static RenderType shieldLuckyCharm() {
        return SHIELD_LUCKY_CHARM;
    }
}
