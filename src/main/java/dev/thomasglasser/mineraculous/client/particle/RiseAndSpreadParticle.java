package dev.thomasglasser.mineraculous.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class RiseAndSpreadParticle extends TextureSheetParticle {
    public RiseAndSpreadParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        lifetime = 30;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.age < this.lifetime / 2.0) {
            move((random.nextBoolean() ? -1 : 1) * random.nextFloat() / 8, 0.5D, (random.nextBoolean() ? -1 : 1) * random.nextFloat() / 8);
        } else {
            move(random.nextFloat() * 2.0F - 1.0F, 0.0D, random.nextFloat() * 2.0F - 1.0F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed) {
            RiseAndSpreadParticle particle = new RiseAndSpreadParticle(level, x, y, z);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
