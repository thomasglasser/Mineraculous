package dev.thomasglasser.mineraculous.api.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public class MineraculousParticleRenderTypes {
    public static final ParticleRenderType PARTICLE_SHEET_ADDITIVE_TRANSLUCENT = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator buffer, TextureManager textureManager) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            boolean fabulous = Minecraft.getInstance().options.graphicsMode().get() == GraphicsStatus.FABULOUS;
            RenderSystem.enableBlend();
            if (fabulous)
                RenderSystem.blendFunc(
                        GlStateManager.SourceFactor.SRC_ALPHA.value,
                        GlStateManager.DestFactor.ONE.value);
            RenderSystem.depthMask(false);
            RenderSystem.enableCull();
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableDepthTest();

            return buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return Mineraculous.MOD_ID + ":PARTICLE_SHEET_ADDITIVE_TRANSLUCENT";
        }
    };
}
