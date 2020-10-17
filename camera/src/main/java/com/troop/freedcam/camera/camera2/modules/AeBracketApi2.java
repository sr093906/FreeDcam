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

package com.troop.freedcam.camera.camera2.modules;

import android.annotation.TargetApi;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;

import com.troop.freedcam.R;

import com.troop.freedcam.utils.ContextApplication;
import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.camera.basecamera.modules.ModuleHandlerAbstract;
import com.troop.freedcam.camera.basecamera.parameters.AbstractParameter;
import com.troop.freedcam.settings.SettingKeys;
import com.troop.freedcam.utils.Log;

/**
 * Created by troop on 17.08.2016.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AeBracketApi2 extends PictureModuleApi2
{

    private final String TAG = AeBracketApi2.class.getSimpleName();

    private final int WAIT_FOR_EXPO_SET = 2;
    private final int WAIT_NOTHING = 3;
    private int WAIT_EXPOSURE_STATE = WAIT_NOTHING;
    long currentExposureTime = 0;
    long exposureTimeStep = 0;
    private boolean aeWasOn = false;
    int maxiso;
    int currentiso;


    public AeBracketApi2(CameraControllerInterface cameraUiWrapper, Handler mBackgroundHandler, Handler mainHandler) {
        super(cameraUiWrapper,mBackgroundHandler,mainHandler);
        name = ContextApplication.getStringFromRessources(R.string.module_hdr);
    }

    @Override
    public String ShortName() {
        return "AeBracket";
    }

    @Override
    public String LongName() {
        return "Ae-Bracket";
    }

    @Override
    public void InitModule() {
        super.InitModule();
        cameraUiWrapper.getParameterHandler().get(SettingKeys.M_Burst).setViewState(AbstractParameter.ViewState.Hidden);
        cameraUiWrapper.getParameterHandler().get(SettingKeys.M_Burst).SetValue(2, true);
        changeCaptureState(ModuleHandlerAbstract.CaptureStates.image_capture_stop);
    }

    @Override
    public void DestroyModule() {
        super.DestroyModule();
        cameraUiWrapper.getParameterHandler().get(SettingKeys.M_Burst).setViewState(AbstractParameter.ViewState.Visible);

    }

    @Override
    protected void onStartTakePicture() {
        maxiso = cameraHolder.characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE).getUpper();
        currentExposureTime = cameraUiWrapper.cameraBackroundValuesChangedListner.currentExposureTime;
        currentiso = cameraUiWrapper.cameraBackroundValuesChangedListner.currentIso;
        exposureTimeStep = currentExposureTime/2;
        String aemode = cameraUiWrapper.parametersHandler.get(SettingKeys.ExposureMode).GetStringValue();
        aeWasOn = !aemode.equals(ContextApplication.getContext().getString(R.string.off));
    }

    @Override
    protected void prepareCaptureBuilder(int captureNum) {
        long expotimeToSet = 0;
        cameraUiWrapper.captureSessionHandler.SetCaptureParameter(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);

        if (currentiso >= maxiso)
            currentiso = maxiso;
        if (currentiso == 0)
            currentiso = 100;
        Log.d(TAG, "set iso to :" + currentiso);
        cameraUiWrapper.captureSessionHandler.SetCaptureParameter(CaptureRequest.SENSOR_SENSITIVITY, currentiso);
        if (0 == captureNum)
            expotimeToSet = currentExposureTime - exposureTimeStep;
        else if (1== captureNum)
            expotimeToSet = currentExposureTime;
        else if (2 == captureNum)
            expotimeToSet = currentExposureTime + exposureTimeStep;
        Log.d(TAG,"Set shutter to:" + expotimeToSet);
        cameraUiWrapper.captureSessionHandler.SetCaptureParameter(CaptureRequest.SENSOR_EXPOSURE_TIME,expotimeToSet);
        //cameraUiWrapper.captureSessionHandler.SetCaptureParameter(CaptureRequest.SENSOR_FRAME_DURATION, expotimeToSet);
        Log.d(TAG, "request: " +captureNum + " AE Mode:" + cameraUiWrapper.captureSessionHandler.getImageCaptureParameter(CaptureRequest.CONTROL_AE_MODE) + " Expotime:" + cameraUiWrapper.captureSessionHandler.getImageCaptureParameter(CaptureRequest.SENSOR_EXPOSURE_TIME) + " iso:" + cameraUiWrapper.captureSessionHandler.getImageCaptureParameter(CaptureRequest.SENSOR_SENSITIVITY));
    }


    @Override
    protected void finishCapture() {
        super.finishCapture();
        Log.d(TAG,"imagecount:" +BurstCounter.getImageCaptured());
        if (BurstCounter.getImageCaptured() == 3) {
            if (aeWasOn && parameterHandler.get(SettingKeys.ExposureMode) != null)
                parameterHandler.get(SettingKeys.ExposureMode).SetValue(ContextApplication.getContext().getString(R.string.on),true);

        }
    }

}