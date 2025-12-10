#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 sampleStep;

out vec4 fragColor;

// Gaussian blur parameters
const int RADIUS = 16;       // Larger radius = rounder blur
const float SIGMA = 7.0;     // Controls softness (higher = rounder)

// Compute Gaussian weight dynamically
float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma));
}

void main() {
    // Center pixel
    float w0 = gaussian(0.0, SIGMA);
    vec4 color = texture(DiffuseSampler, texCoord) * w0;

    float weightSum = w0;

    // Blur in both directions (optimized loop)
    for (int i = 1; i <= RADIUS; i++) {
        float w = gaussian(float(i), SIGMA);

        vec2 offset = sampleStep * float(i);

        color += texture(DiffuseSampler, texCoord + offset) * w;
        color += texture(DiffuseSampler, texCoord - offset) * w;

        weightSum += 2.0 * w;
    }

    // Normalize and give a slight boost like your original
    color = color / weightSum;
    float alphaBoost = 2.0; // increase for stronger outlines
    fragColor = vec4(color.rgb * 2.0, color.a * alphaBoost);
}