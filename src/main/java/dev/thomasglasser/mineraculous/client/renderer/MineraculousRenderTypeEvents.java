package dev.thomasglasser.mineraculous.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.thomasglasser.mineraculous.Mineraculous;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

public class MineraculousRenderTypeEvents {
    public static void onRegisterRenderBuffers(RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(MineraculousRenderTypes.luckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.armorLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.entityLuckyCharm());
        event.registerRenderBuffer(MineraculousRenderTypes.shieldLuckyCharm());
    }

    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            ResourceProvider resourceProvider = event.getResourceProvider();
            event.registerShader(
                    new ShaderInstance(resourceProvider, Mineraculous.modLoc("rendertype_glint_translucent_lightmap"), DefaultVertexFormat.POSITION_TEX_LIGHTMAP_COLOR),
                    instance -> MineraculousRenderStateShards.rendertypeGlintTranslucentLightmapShader = instance);
        } catch (IOException e) {
            Mineraculous.LOGGER.error("Failed to register shaders", e);
        }
    }
}
