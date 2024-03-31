package dev.thomasglasser.mineraculous.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

public class MineraculousParticleRenderTypes
{
	static final ParticleRenderType TRANSLUCENT_FIXED = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {
			RenderSystem.depthMask(true);
			RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tessellator) {
			tessellator.end();
			RenderSystem.enableDepthTest();
		}

		@Override
		public String toString() {
			return Mineraculous.modLoc("translucent_fixed").toString();
		}
	};
}
