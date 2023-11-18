uniform sampler2D map0;
uniform sampler2D map1;
uniform sampler2D map2;
uniform sampler2D map3;

varying vec2 texCoord0;
varying vec2 texCoord1;
varying vec2 texCoord2;
varying vec2 texCoord3;

varying vec4 vertexColor;

void main(void)
{
	vec4 col0 = texture2D(map0, texCoord0);
	vec4 col1 = texture2D(map1, texCoord1);
	vec4 col2 = texture2D(map2, texCoord2);
	vec4 col3 = col2/2.5;
	vec4 blend = texture2D(map3, texCoord3);
	
	gl_FragColor = vertexColor * mix(mix(mix(col0, col1, blend.r), col2, blend.g), col3, blend.b);
}