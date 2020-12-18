package freed.gl;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.widget.RelativeLayout;

public class GLPreview extends GLSurfaceView {
    MainRenderer mRenderer;
    private int mRatioWidth;
    private int mRatioHeight;
    private TextureView.SurfaceTextureListener surfaceTextureListener;

    public GLPreview(Context context) {
        super(context);
        init();
    }

    public GLPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRenderer = new MainRenderer(this);
        setEGLContextClientVersion(2);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void fireOnSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int w, int h)
    {
        if (surfaceTextureListener != null)
            surfaceTextureListener.onSurfaceTextureAvailable(surfaceTexture,w,h);
    }

    public void fireOnSurfaceTextureDestroyed(SurfaceTexture surfaceTexture)
    {
        if (surfaceTextureListener != null)
            surfaceTextureListener.onSurfaceTextureDestroyed(surfaceTexture);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        super.surfaceChanged(holder, format, w, h);
        if (surfaceTextureListener != null)
            surfaceTextureListener.onSurfaceTextureSizeChanged(getSurfaceTexture(),w,h);
    }

    @Override
    public void onResume() {
        super.onResume();
        //mRenderer.onResume();
    }

    @Override
    public void onPause() {
        fireOnSurfaceTextureDestroyed(getSurfaceTexture());
        //mRenderer.onPause();
        super.onPause();
    }


    public SurfaceTexture getSurfaceTexture()
    {
        return mRenderer.getmSTexture();
    }


    public void setSurfaceTextureListener(TextureView.SurfaceTextureListener l) {
        this.surfaceTextureListener = l;
    }

    public void scale(int in_width, int in_height, int out_width)
    {
        RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(in_width, in_height);
        layout.height = in_height;
        layout.width = in_width;
        layout.leftMargin = (out_width - in_width)/2;
        setLayoutParams(layout);
    }

    public void setOrientation(int or)
    {
        mRenderer.setOrientation(or);
    }
}
