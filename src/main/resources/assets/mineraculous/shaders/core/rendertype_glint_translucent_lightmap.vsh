#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV0;
in ivec2 UV2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 TextureMat;
uniform int FogShape;
uniform sampler2D Sampler2;

out float vertexDistance;
out vec2 texCoord0;
out vec4 lightMapColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
    lightMapColor = texelFetch(Sampler2, UV2 / 16, 0);
}
