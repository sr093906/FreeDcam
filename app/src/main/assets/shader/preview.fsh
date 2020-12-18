#version 300 es
#line 1
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
uniform samplerExternalOES sTexture;
out vec4 Output;
uniform mat4 uTexRotateMatrix;
void main() {
    vec2 texSize = vec2(textureSize(sTexture, 0));
    vec2 posScaled = (vec2(gl_FragCoord.xy));
    vec2 pos = posScaled/texSize;
    pos.y = 1.0-pos.y;
    Output = texture(sTexture,pos);
}