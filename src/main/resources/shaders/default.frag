#version 150 core

in vec4 vertexColor;
in vec2 textureCoord;
in float isTexture;

out vec4 fragColor;

uniform sampler2D texImage;

void main() {
    vec4 textureColor;
    if (isTexture == 1.0f) {
        textureColor = texture(texImage, textureCoord);
    } else {
        textureColor = vec4(1.0, 1.0, 1.0, 1.0);
    }

    fragColor = vertexColor * textureColor;
    if (fragColor.w < 1.0)
        discard;
}