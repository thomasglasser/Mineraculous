package dev.thomasglasser.mineraculous.api.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

/**
 * Forces all rendered vertices to be written using a fixed RGB color,
 * ignoring any per-vertex color values supplied during rendering.
 */
public class ColoredOutlineBufferSource implements MultiBufferSource, AutoCloseable {
    private final MultiBufferSource.BufferSource bufferSource;
    private int color;

    public ColoredOutlineBufferSource(BufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        VertexConsumer delegate = this.bufferSource.getBuffer(renderType);
        return new EntityOutlineGenerator(delegate, this.color);
    }

    @Override
    public void close() {
        this.bufferSource.endBatch();
    }

    public record EntityOutlineGenerator(VertexConsumer delegate, int color) implements VertexConsumer {
        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            final int r = (color >> 16) & 0xFF;
            final int g = (color >> 8) & 0xFF;
            final int b = (color >> 0) & 0xFF;
            final int a = 0xFF;
            this.delegate.setColor(r, g, b, a);
            return this;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.delegate.addVertex(x, y, z);
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
