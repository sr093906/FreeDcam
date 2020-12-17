package freed.gl;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {


    private SurfaceTexture mSTexture;

    private boolean mGLInit = false;
    private boolean mUpdateST = false;

    private GLPreview mView;
    private PreviewShape previewShape;

    MainRenderer(GLPreview view) {
        mView = view;
        previewShape = new PreviewShape();
    }


    public void onDrawFrame(GL10 unused) {
        if (!mGLInit) return;
        //GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        synchronized (this) {
            if (mUpdateST) {
                mSTexture.updateTexImage();
                mUpdateST = false;
            }
        }
        previewShape.draw();
        //GLES20.glFlush();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSTexture = new SurfaceTexture(previewShape.create()[0]);
        mSTexture.setOnFrameAvailableListener(this);
        mGLInit = true;
        mView.fireOnSurfaceTextureAvailable(mSTexture,0,0);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }


    public SurfaceTexture getmSTexture()
    {
        return mSTexture;
    }


    public synchronized void onFrameAvailable(SurfaceTexture st) {
        mUpdateST = true;
        mView.requestRender();
    }
}
