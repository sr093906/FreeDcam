/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package com.troop.freedcam.camera.camera1.modules;

import android.hardware.Camera;
import android.os.Handler;

import com.troop.freedcam.camera.R;
import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.camera.basecamera.parameters.AbstractParameter;
import com.troop.freedcam.camera.basecamera.parameters.ParameterInterface;
import com.troop.freedcam.eventbus.enums.CaptureStates;
import com.troop.freedcam.file.holder.BaseHolder;
import com.troop.freedcam.settings.Frameworks;
import com.troop.freedcam.settings.SettingKeys;
import com.troop.freedcam.settings.SettingsManager;
import com.troop.freedcam.utils.ContextApplication;
import com.troop.freedcam.utils.Log;

import java.io.File;
import java.util.Date;


/**
 * Created by troop on 16.08.2014.
 */
public class BracketModule extends PictureModule {

    private final String TAG = BracketModule.class.getSimpleName();

    private int hdrCount;
    private BaseHolder[] files;

    public BracketModule(CameraControllerInterface cameraUiWrapper, Handler mBackgroundHandler, Handler mainHandler) {
        super(cameraUiWrapper, mBackgroundHandler, mainHandler);
        name = ContextApplication.getStringFromRessources(R.string.module_hdr);
    }

    //ModuleInterface START
    @Override
    public String ModuleName() {
        return name;
    }

    @Override
    public void DoWork() {
        mBackgroundHandler.post(() -> {
            if (SettingsManager.getGlobal(SettingKeys.LOCATION_MODE).get().equals(ContextApplication.getStringFromRessources(com.troop.freedcam.camera.R.string.on_)))
                cameraHolder.SetLocation(cameraUiWrapper.getActivityInterface().getLocationManager().getCurrentLocation());
            files = new BaseHolder[3];
            hdrCount = 0;
            String picformat = cameraUiWrapper.getParameterHandler().get(SettingKeys.PictureFormat).GetStringValue();
            if (picformat.equals(ContextApplication.getStringFromRessources(R.string.dng_)) || picformat.equals(ContextApplication.getStringFromRessources(R.string.bayer_))) {
                ParameterInterface zsl = cameraUiWrapper.getParameterHandler().get(SettingKeys.ZSL);
                if (zsl != null && zsl.getViewState() == AbstractParameter.ViewState.Visible
                        && zsl.GetStringValue().equals("on")
                        && (SettingsManager.getInstance().getFrameWork() != Frameworks.MTK))
                    zsl.SetValue("off", true);
            }
            changeCaptureState(CaptureStates.image_capture_start);
            waitForPicture = true;

            setExposureToCamera();
            sleep(400);
            startcapturetime = new Date().getTime();
            cameraHolder.TakePicture(BracketModule.this);

        });
    }

    @Override
    public String ShortName() {
        return "Bracket";
    }

    @Override
    public String LongName() {
        return "Bracketing";
    }

    @Override
    public boolean IsWorking() {
        return isWorking;
    }

    //ModuleInterface END

    private void setExposureToCamera() {
        int value = 0;
        if (hdrCount == 0) {
            value = -2;
        } else if (hdrCount == 1)
            value = 0;
        else if (hdrCount == 2)
            value = 2;

        Log.d(TAG, "Set HDR Exposure to :" + value + "for image count " + hdrCount);
        int toset = value + cameraUiWrapper.getParameterHandler().get(SettingKeys.M_ExposureCompensation).getStringValues().length / 2;
        cameraUiWrapper.getParameterHandler().get(SettingKeys.M_ExposureCompensation).SetValue(toset, true);
        Log.d(TAG, "HDR Exposure SET");
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (data == null)
            return;
        if (!waitForPicture) {
            isWorking = false;
            return;
        }
        hdrCount++;
        String picFormat = cameraUiWrapper.getParameterHandler().get(SettingKeys.PictureFormat).GetStringValue();
        saveImage(data, picFormat);
        startPreview();
        if (hdrCount == 3)//handel normal capture
        {
            waitForPicture = false;
            isWorking = false;
            changeCaptureState(CaptureStates.image_capture_stop);
            setExposureToCamera();
            fireOnWorkFinish(files);
        } else {
            setExposureToCamera();
            sleep(600);
            startcapturetime = new Date().getTime();
            cameraHolder.TakePicture(BracketModule.this);
        }
        data = null;
    }

    @Override
    protected File getFile(String fileending) {
        return new File(cameraUiWrapper.getActivityInterface().getFileListController().getStorageFileManager().getNewFilePathHDR(SettingsManager.getInstance().GetWriteExternal(), fileending, hdrCount));
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {
            Log.WriteEx(ex);
        }
    }

    @Override
    public void internalFireOnWorkDone(BaseHolder file) {
        files[hdrCount - 1] = file;
    }

}
