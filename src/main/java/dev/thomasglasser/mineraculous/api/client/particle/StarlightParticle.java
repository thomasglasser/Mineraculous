package dev.thomasglasser.mineraculous.api.client.particle;

import dev.thomasglasser.mineraculous.api.client.renderer.MineraculousParticleRenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class StarlightParticle extends TextureSheetParticle {
    public StarlightParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        scale(level.random.nextInt(1, 7) / 5f);
        this.lifetime = 60;
        float r = (float) Math.random();
        this.oRoll = r;
        this.roll = r;
    }

    @Override
    public void tick() {
        this.alpha = ((float) (this.lifetime - this.age)) / (float) this.lifetime;
        this.move(0, 0.01, 0);

        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MineraculousParticleRenderTypes.ADDITIVE_PARTICLE;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            StarlightParticle particle = new StarlightParticle(level, x, y, z);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
