varying vec2 texCoord0;
varying vec2 texCoord1;
varying vec2 texCoord2;
varying vec2 texCoord3;

varying vec4 vertexColor;

uniform mat4 splatTransform;

const vec4 WHITE = vec4(1.0,1.0,1.0,1.0);

void main(void)
{
	texCoord0=gl_MultiTexCoord0.xy;
	texCoord1=gl_MultiTexCoord1.xy*0.5;
	texCoord2=gl_MultiTexCoord2.xy;
	
	texCoord3=(splatTransform * vec4(gl_MultiTexCoord3.xy, 0.0, 1.0)).xy;
	
	vertexColor = (gl_FrontLightModelProduct.sceneColor * gl_FrontMaterial.ambient) + 
	(gl_LightSource[0].ambient * gl_FrontMaterial.ambient);
	
	vec3 vVertex = vec3(gl_ModelViewMatrix * gl_Vertex);
	vec3 normalEye   = normalize(gl_ModelViewMatrix * vec4(gl_Normal, 0.0)).xyz;
	float angle = dot(normalEye, normalize(gl_LightSource[0].position.xyz - vVertex.xyz));
		
	if (angle > 0.0) {
		vertexColor += vec4(gl_LightSource[0].diffuse * angle + gl_LightSource[0].specular * pow(angle, gl_FrontMaterial.shininess));
	}
		
	vertexColor=vec4(min(WHITE, vertexColor).xyz, 1.0);
	gl_Position = ftransform();
}