#version 330 core

layout (location = 0) in vec3 position;

uniform mat4 pr_matrix;
uniform mat4 vw_matrix;
uniform vec4 t_color;
uniform mat4 ml_matrix;

out DATA
{
    vec4 tc;
} vs_out;

void main()
{
    gl_Position = pr_matrix * vw_matrix * ml_matrix * vec4(position, 1.0);
    vs_out.tc = t_color;
}