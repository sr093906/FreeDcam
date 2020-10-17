package com.troop.freedcam.camera.camera1.parameters.manual.shutter;

import android.hardware.Camera;

import com.troop.freedcam.R;

import com.troop.freedcam.utils.ContextApplication;
import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.camera.basecamera.parameters.AbstractParameter;
import com.troop.freedcam.camera.camera1.parameters.ParametersHandler;
import com.troop.freedcam.settings.SettingKeys;
import com.troop.freedcam.settings.SettingsManager;
import com.troop.freedcam.utils.Log;

/**
 * Created by troop on 29.01.2017.
 */

public class ExposureTime_MS extends AbstractParameter {
    private final String TAG = ExposureTime_MS.class.getSimpleName();
    private Camera.Parameters parameters;
    public ExposureTime_MS(CameraControllerInterface cameraUiWrapper, Camera.Parameters parameters, SettingKeys.Key settingMode) {
        super(cameraUiWrapper,settingMode);
        this.parameters = parameters;
        setViewState(ViewState.Visible);
    }

    @Override
    public void setValue(int valueToset, boolean setToCamera)
    {
        super.setValue(valueToset,setToCamera);
        currentInt = valueToset;
        String shutterstring = stringvalues[currentInt];
        if(!shutterstring.equals(ContextApplication.getStringFromRessources(R.string.auto_)))
        {
            if (stringvalues[currentInt].contains("/")) {
                String[] split = stringvalues[currentInt].split("/");
                Double a = Double.parseDouble(split[0]) / Double.parseDouble(split[1]);
                shutterstring = "" + a;
            }
            shutterstring = Double.parseDouble(shutterstring) * 1000 +"";
            Log.d(TAG, "set exposure time to " + shutterstring);
            parameters.set(SettingsManager.get(SettingKeys.M_ExposureTime).getCamera1ParameterKEY(), shutterstring);
        }
        else
        {
            parameters.set(SettingsManager.get(SettingKeys.M_ExposureTime).getCamera1ParameterKEY(), "0");
            Log.d(TAG, "set exposure time to auto");
        }
        ((ParametersHandler) cameraUiWrapper.getParameterHandler()).SetParametersToCamera(parameters);
    }
}