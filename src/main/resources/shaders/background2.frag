#version 330 core

layout (location = 0) out vec4 color;

in DATA
{
	vec3 position;
} fs_in;

void main()
{
	color = vec4(45, 45, 45, 45);
}