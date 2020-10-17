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

package com.troop.freedcam.camera.camera1.parameters.manual;

import android.hardware.Camera.Parameters;

import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.camera.basecamera.parameters.AbstractParameter;
import com.troop.freedcam.camera.camera1.parameters.ParametersHandler;
import com.troop.freedcam.settings.SettingKeys;
import com.troop.freedcam.settings.SettingsManager;
import com.troop.freedcam.settings.mode.SettingMode;
import com.troop.freedcam.utils.Log;

/**
 * Created by troop on 17.08.2014.
 */
public class BaseManualParameter extends AbstractParameter
{

    private final String TAG = BaseManualParameter.class.getSimpleName();
    /**
     * Holds the list of Supported parameters
     */
    protected Parameters  parameters;
    /*
     * The name of the current key_value to get like brightness
     */
    protected String key_value;




    public BaseManualParameter(Parameters parameters, CameraControllerInterface cameraUiWrapper, SettingKeys.Key settingMode)
    {
        super(cameraUiWrapper,settingMode);
        this.parameters = parameters;
        SettingMode mode = (SettingMode) SettingsManager.get(key);
        key_value = mode.getCamera1ParameterKEY();
        currentString = mode.get();
        stringvalues = mode.getValues();
        if (mode.isSupported())
            setViewState(ViewState.Visible);
    }

    @Override
    public void setValue(int valueToset, boolean setToCamera)
    {
        currentInt = valueToset;
        Log.d(TAG, "set " + key_value + " to " + valueToset);
        if(stringvalues == null || stringvalues.length == 0)
            return;
        settingMode.set(String.valueOf(valueToset));
        parameters.set(key_value, stringvalues[valueToset]);
        //fireIntValueChanged(valueToset);
        fireStringValueChanged(stringvalues[valueToset]);
        try
        {
            Log.d(TAG,"SetValue " + key_value);
            ((ParametersHandler) cameraUiWrapper.getParameterHandler()).SetParametersToCamera(parameters);
        }
        catch (Exception ex)
        {
            Log.WriteEx(ex);
        }
    }
}