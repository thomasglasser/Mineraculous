package dev.thomasglasser.mineraculous.api.client.particle;

import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousParticleRenderTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public class RevertingLadybugParticle extends TextureSheetParticle {
    public RevertingLadybugParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        scale(level.random.nextInt(1, MineraculousClientConfig.get().size.getAsInt()) / 6f);
        this.lifetime = level.random.nextInt(30, 60);
        float r = (float) Math.random();
        this.oRoll = r;
        this.roll = r;
    }

    @Override
    public void tick() {
        scale((this.lifetime - this.age) / (float) this.lifetime * 1.2f);
        this.move(0, 0.003, 0);
        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MineraculousParticleRenderTypes.PARTICLE_SHEET_ADDITIVE_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            RevertingLadybugParticle particle = new RevertingLadybugParticle(level, x, y, z);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
