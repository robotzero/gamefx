// Basic Texture Shader
#version 330 core

//layout (location = 0) in vec3 position;

//layout(location = 0) in vec3 a_Position;
//layout(location = 1) in vec4 a_Color;
//layout(location = 2) in vec2 a_TexCoord;
//layout(location = 3) in float a_TexIndex;
//layout(location = 4) in float a_TilingFactor;

uniform mat4 u_ViewProjection;
uniform mat4 vw_matrix;
uniform mat4 ml_matrix;

//out vec4 v_Color;
//out vec2 v_TexCoord;
//out float v_TexIndex;
//out float v_TilingFactor;

void main()
{
//	v_Color = a_Color;
//	v_TexCoord = a_TexCoord;
//	v_TexIndex = a_TexIndex;
//	v_TilingFactor = a_TilingFactor;
//	gl_Position = u_ViewProjection * vec4(a_Position, 1.0);
	//gl_Position = u_ViewProjection * vec4(vec3(20, 20, 0), 1.0);
	gl_Position = u_ViewProjection * vw_matrix * ml_matrix * vec4(vec3(200, 300, 0), 1.0);
}