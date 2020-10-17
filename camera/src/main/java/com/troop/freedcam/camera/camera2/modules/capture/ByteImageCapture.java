package com.troop.freedcam.camera.camera2.modules.capture;

import android.media.Image;
import android.os.Build;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.nio.ByteBuffer;

import freed.ActivityInterface;
import com.troop.freedcam.camera.basecamera.modules.ModuleInterface;
import freed.image.ImageSaveTask;

import com.troop.freedcam.image.ImageTask;
import com.troop.freedcam.utils.Log;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ByteImageCapture extends StillImageCapture {

    private final String TAG = ByteImageCapture.class.getSimpleName();
    public ByteImageCapture(Size size, int format, boolean setToPreview, ActivityInterface activityInterface, ModuleInterface moduleInterface, String file_ending,int max_images) {
        super(size, format, setToPreview,activityInterface,moduleInterface,file_ending,max_images);
    }

    @Override
    public ImageTask getSaveTask() {
        return super.getSaveTask();
    }

    @Override
    protected void createTask() {
        if (result == null || image == null)
            return;
        File file = new File(getFilepath()+file_ending);
        task = process_jpeg(image, file);
        image.close();
        image = null;
    }

    private ImageTask process_jpeg(Image image, File file) {

        Log.d(TAG, "Create JPEG");
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        ImageSaveTask task = new ImageSaveTask(activityInterface,moduleInterface);
        task.setBytesTosave(bytes, ImageSaveTask.JPEG);
        task.setFilePath(file,externalSD);
        buffer.clear();
        image.close();
        buffer = null;
        image = null;
        return task;
    }
}