package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MineraculousRenderTypes {
    private static final ResourceLocation LUCKY_CHARM_LOCATION = Mineraculous.modLoc("textures/misc/lucky_charm.png");
    private static final RenderType LUCKY_CHARM = RenderType.create(
            "mineraculous:lucky_charm",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(LUCKY_CHARM_LOCATION, true, false))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTexturingState(RenderStateShard.GLINT_TEXTURING)
                    .createCompositeState(false));
    public static final RenderType ARMOR_LUCKY_CHARM = RenderType.create(
            "armor_lucky_charm",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(LUCKY_CHARM_LOCATION, true, false))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .createCompositeState(false));

    public static RenderType luckyCharm() {
        return LUCKY_CHARM;
    }

    public static RenderType armorLuckyCharm() {
        return ARMOR_LUCKY_CHARM;
    }
}
