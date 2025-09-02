package dev.thomasglasser.mineraculous.api.client.renderer;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.entity.vehicle.Minecart;

public class MineraculousParticleRenderTypes {
    public static final ParticleRenderType PARTICLE_SHEET_ADDITIVE_TRANSLUCENT = new ParticleRenderType() {
        @Override
        public BufferBuilder begin(Tesselator tesselator, TextureManager textureManager) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.enableBlend();
            RenderSystem.depthMask(false);
            RenderSystem.blendEquation(GlConst.GL_FUNC_REVERSE_SUBTRACT);
            RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.ONE_MINUS_CONSTANT_ALPHA.value,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value);
            RenderSystem.enableCull();
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableDepthTest();
            RenderSystem.blendEquation(GlConst.GL_FUNC_ADD);
            return tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public String toString() {
            return Mineraculous.MOD_ID + ":PARTICLE_SHEET_ADDITIVE_TRANSLUCENT";
        }
    };
}
