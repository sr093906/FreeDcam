package com.troop.freedcam.camera.camera2.modules.capture;

import android.os.Build;
import android.util.Size;

import androidx.annotation.RequiresApi;

import com.troop.freedcam.camera.basecamera.modules.ModuleInterface;
import com.troop.freedcam.camera.image.EmptyTask;
import com.troop.freedcam.file.FileListController;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ContinouseYuvCapture extends StillImageCapture {

    public ContinouseYuvCapture(Size size, int format, boolean setToPreview, FileListController activityInterface, ModuleInterface moduleInterface, String file_ending, int max_images) {
        super(size, format, setToPreview, activityInterface, moduleInterface, file_ending,max_images);
    }


    @Override
    protected void createTask() {
        task = new EmptyTask();
        if (image !=  null)
            image.close();
    }
}
