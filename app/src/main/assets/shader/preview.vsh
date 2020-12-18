#version 300 es
#line 1
in vec2 vPosition;
uniform mat4 uTexRotateMatrix;
void main() {
    gl_Position = uTexRotateMatrix * vec4(vPosition.x, vPosition.y, 0.0, 1.0);
}