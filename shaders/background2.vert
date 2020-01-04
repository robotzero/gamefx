#version 330 core

layout (location = 0) in vec4 position;

uniform mat4 pr_matrix;
uniform mat4 vw_matrix;

out DATA
{
	vec3 position;
} vs_out;

void main()
{
	gl_Position = pr_matrix * vw_matrix * position;
	vs_out.position = vec3(vw_matrix * position);
}