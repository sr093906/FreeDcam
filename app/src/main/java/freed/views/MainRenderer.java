package freed.views;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private final String vss_default = "" +
            "attribute vec2 vPosition;\n" +
            "attribute vec4 vTexCoord;\n" +
            "uniform mat4 uTexRotateMatrix;\n" +
            "varying vec2 texCoord;\n" +
            "void main() {\n" +
            "  texCoord = vTexCoord.xy;\n" +
            "  gl_Position = uTexRotateMatrix * vec4 ( vPosition.x, vPosition.y, 0.0, 1.0 );\n" +
            "}";

    private final String fss_default = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "varying vec2 texCoord;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture,texCoord);\n" +
            "}";

    private int[] hTex;
    private FloatBuffer pVertex;
    private FloatBuffer pTexCoord;
    private int hProgram;

    private SurfaceTexture mSTexture;

    private boolean mGLInit = false;
    private boolean mUpdateST = false;

    private GLPreview mView;
    private float[] mTexRotateMatrix = new float[] {1, 0, 0, 0,   0, 1, 0, 0,   0, 0, 1, 0,   0, 0, 0, 1};

    MainRenderer(GLPreview view) {
        mView = view;
        float[] vtmp = {1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f};
        float[] ttmp = {1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f};
        pVertex = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pVertex.put(vtmp);
        pVertex.position(0);
        pTexCoord = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        pTexCoord.put(ttmp);
        pTexCoord.position(0);
    }


    public void onDrawFrame(GL10 unused) {
        if (!mGLInit) return;
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        synchronized (this) {
            if (mUpdateST) {
                mSTexture.updateTexImage();
                mUpdateST = false;
            }
        }

        GLES20.glUseProgram(hProgram);
        int trmh = GLES20.glGetUniformLocation ( hProgram, "uTexRotateMatrix" );
        GLES20.glUniformMatrix4fv(trmh, 1, false, mTexRotateMatrix, 0);

        int ph = GLES20.glGetAttribLocation(hProgram, "vPosition");
        int tch = GLES20.glGetAttribLocation(hProgram, "vTexCoord");

        GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 4 * 2, pVertex);
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 4 * 2, pTexCoord);
        GLES20.glEnableVertexAttribArray(ph);
        GLES20.glEnableVertexAttribArray(tch);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(hProgram, "sTexture"), 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glFlush();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initTex();
        mSTexture = new SurfaceTexture(hTex[0]);
        mSTexture.setOnFrameAvailableListener(this);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        hProgram = loadShader(vss_default, fss_default);

        mGLInit = true;
        mView.fireOnSurfaceTextureAvailable(mSTexture,0,0);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }


    public SurfaceTexture getmSTexture()
    {
        return mSTexture;
    }

    private void initTex() {
        hTex = new int[1];
        GLES20.glGenTextures(1, hTex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, hTex[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    }

    public synchronized void onFrameAvailable(SurfaceTexture st) {
        mUpdateST = true;
        mView.requestRender();
    }

    private static int loadShader(String vss, String fss) {
        int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vshader, vss);
        GLES20.glCompileShader(vshader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile vshader");
            Log.v("Shader", "Could not compile vshader:" + GLES20.glGetShaderInfoLog(vshader));
            GLES20.glDeleteShader(vshader);
            vshader = 0;
        }

        int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fshader, fss);
        GLES20.glCompileShader(fshader);
        GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Shader", "Could not compile fshader");
            Log.v("Shader", "Could not compile fshader:" + GLES20.glGetShaderInfoLog(fshader));
            GLES20.glDeleteShader(fshader);
            fshader = 0;
        }

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vshader);
        GLES20.glAttachShader(program, fshader);
        GLES20.glLinkProgram(program);

        return program;
    }

    public void setOrientation(int or)
    {
        android.opengl.Matrix.setRotateM(mTexRotateMatrix, 0,  or, 0f, 0f, 1f);
    }


    RectF mLastImageRect = new RectF();
    RectF inputRect = new RectF();
    public void scale(int in_width, int in_height, int out_width, int out_height, int rotation)
    {
        int difw = out_width - in_width;
        int difh = out_height - in_height;

        inputRect.left = difw/2;
        inputRect.top = difh/2;
        inputRect.right = in_width;
        inputRect.bottom = in_height;
        if (mLastImageRect != inputRect) {
            GLES20.glViewport((int) inputRect.left, (int) inputRect.top, (int) inputRect.width(), (int) inputRect.height());

            mLastImageRect.set(inputRect);
        }

    }
}
