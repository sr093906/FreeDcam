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

package com.troop.freedcam.camera.camera1.parameters.modes;

import android.hardware.Camera.Parameters;

import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.camera.camera1.parameters.ParametersHandler;
import com.troop.freedcam.settings.SettingKeys;
import com.troop.freedcam.settings.SettingsManager;
import com.troop.freedcam.utils.Log;

/**
 * Created by troop on 18.08.2014.
 */
public class PictureSizeParameter extends BaseModeParameter
{
    final String TAG = PictureSizeParameter.class.getSimpleName();
    public PictureSizeParameter(Parameters  parameters, CameraControllerInterface parameterChanged) {
        super(parameters, parameterChanged, SettingKeys.PictureSize);
        this.cameraUiWrapper = parameterChanged;
        setViewState(ViewState.Visible);
    }

    @Override
    public void SetValue(String valueToSet, boolean setToCam)
    {
        parameters.set("picture-size" , valueToSet);
        SettingsManager.get(SettingKeys.PictureSize).set(valueToSet);
        currentString = valueToSet;
        Log.d(TAG, "SetValue : picture-size");
        if (setToCam)
            ((ParametersHandler) cameraUiWrapper.getParameterHandler()).SetParametersToCamera(parameters);
        fireStringValueChanged(valueToSet);

    }

    @Override
    public String GetStringValue() {
        return SettingsManager.get(SettingKeys.PictureSize).get();
    }

    @Override
    public String[] getStringValues() {
        return SettingsManager.get(SettingKeys.PictureSize).getValues();
    }
}