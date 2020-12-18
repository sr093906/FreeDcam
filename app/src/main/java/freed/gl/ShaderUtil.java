package freed.gl;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import freed.FreedApplication;

public class ShaderUtil {

    private static final String TAG = ShaderUtil.class.getSimpleName();

    private static String getShader(String name) throws IOException {
        Context context = FreedApplication.getContext();
        try (InputStream stream = context.getAssets().open(name, AssetManager.ACCESS_BUFFER))
        {
            byte[] result = new byte[stream.available()];
            int bytesRead = stream.read(result);
            return new String(result, Charset.defaultCharset());
        }
    }

    public static String getPreviewVertex()
    {
        try {
            return getShader("shader/preview.vsh");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPreviewFragment()
    {
        try {
            return getShader("shader/preview.fsh");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int createShader(String shader , String shadername, int shaderType)
    {
        int[] compiled = new int[1];
        int fshader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(fshader, shader);
        GLES20.glCompileShader(fshader);
        GLES20.glGetShaderiv(fshader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shadername);
            Log.v(TAG, "Could not compile shader: " + shadername + " " + GLES20.glGetShaderInfoLog(fshader));
            GLES20.glDeleteShader(fshader);
            fshader = 0;
        }
        return fshader;
    }
}
