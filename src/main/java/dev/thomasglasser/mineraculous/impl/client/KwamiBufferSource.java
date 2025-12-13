package dev.thomasglasser.mineraculous.impl.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class KwamiBufferSource implements MultiBufferSource, AutoCloseable {
    private final MultiBufferSource.BufferSource kwamiBufferSource;
    private int color;

    public KwamiBufferSource(BufferSource bufferSource) {
        kwamiBufferSource = bufferSource;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        VertexConsumer delegate = this.kwamiBufferSource.getBuffer(renderType);
        return new KwamiOutlineGenerator(delegate, this.color);
    }

    @Override
    public void close() {
        this.kwamiBufferSource.endBatch();
    }

    public static record KwamiOutlineGenerator(VertexConsumer delegate, int color) implements VertexConsumer {

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.delegate.addVertex(x, y, z).setColor(this.color);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            this.delegate.setNormal(normalX, normalY, normalZ);
            return this;
        }
    }
}
