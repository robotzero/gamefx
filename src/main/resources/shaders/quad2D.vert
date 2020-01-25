#version 330 core

layout (location = 0) in vec4 position;

uniform mat4 pr_matrix;
uniform mat4 vw_matrix;
uniform vec4 t_color;

out DATA
{
    vec4 tc;
} vs_out;

void main()
{
    gl_Position = pr_matrix * vw_matrix * position;
    vs_out.tc = t_color;
}