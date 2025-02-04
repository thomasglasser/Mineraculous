package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class MineraculousRenderTypes {
    private static final RenderType LUCKY_CHARM = RenderType.create(
            "lucky_charm",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(Mineraculous.modLoc("textures/misc/lucky_charm.png"), true, false))
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
//                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
                    .setTexturingState(RenderStateShard.GLINT_TEXTURING)
                    .createCompositeState(false));

    public static RenderType luckyCharm() {
        return LUCKY_CHARM;
    }
}
