package dev.thomasglasser.mineraculous.client.particle;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class CataclysmParticle extends TextureSheetParticle
{
	public CataclysmParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed)
	{
		super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
		if (MineraculousClientUtils.isFirstPerson())
			scale(0.1f);
		else
			scale(0.3f);
		this.friction = 0.96F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		this.lifetime = 3;
	}

	@Override
	public @NotNull ParticleRenderType getRenderType()
	{
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType>
	{
		private final SpriteSet sprites;

		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			CataclysmParticle particle = new CataclysmParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
			particle.pickSprite(sprites);
			return particle;
		}
	}
}
