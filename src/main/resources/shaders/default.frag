#version 150 core

in vec4 vertexColor;
in vec2 textureCoord;

out vec4 fragColor;

uniform sampler2D texImage;
uniform bool isTexture = false;

void main() {
    vec4 textureColor = vec4(1f, 1f, 1f, 1f);
    if (isTexture == true) {
        textureColor = texture(texImage, textureCoord);
    }
    fragColor = vertexColor * textureColor;
    if (fragColor.w < 1.0)
        discard;
}