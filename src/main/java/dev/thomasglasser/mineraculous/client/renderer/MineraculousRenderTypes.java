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

    public static final RenderStateShard.TexturingStateShard LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "lucky_charm_texturing", () -> setupLuckyCharmTexturing(8.0F), () -> RenderSystem.resetTextureMatrix());

    public static final RenderStateShard.TexturingStateShard ENTITY_LUCKY_CHARM_TEXTURING = new RenderStateShard.TexturingStateShard(
            "entity_lucky_charm_texturing", () -> setupLuckyCharmTexturing(0.16F), () -> RenderSystem.resetTextureMatrix());

    public static void setupLuckyCharmTexturing(float scale) {
        float speed = 1 / 100f;
        long i = (long) ((double) Util.getMillis() * speed * 8.0);
        float f = (float) (i % 110000L) / 110000.0F;
        float f1 = (float) (i % 30000L) / 30000.0F;
        Matrix4f matrix4f = new Matrix4f().translation(-f, f1, 0.0F);
        matrix4f.rotateZ((float) (Math.PI / 18)).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }

    private static final RenderType LUCKY_CHARM = RenderType.create(
            "mineraculous:lucky_charm",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(LUCKY_CHARM_LOCATION, false, true))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTexturingState(LUCKY_CHARM_TEXTURING)
                    .createCompositeState(false));
    public static final RenderType ARMOR_LUCKY_CHARM = RenderType.create(
            "mineraculous:armor_lucky_charm",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(LUCKY_CHARM_LOCATION, false, false))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTexturingState(ENTITY_LUCKY_CHARM_TEXTURING)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false));

    public static RenderType luckyCharm() {
        return LUCKY_CHARM;
    }

    public static RenderType armorLuckyCharm() {
        return ARMOR_LUCKY_CHARM;
    }
}
