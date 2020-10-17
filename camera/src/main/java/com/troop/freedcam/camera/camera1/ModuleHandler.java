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

package com.troop.freedcam.camera.camera1;


import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.camera.basecamera.modules.ModuleHandlerAbstract;
import com.troop.freedcam.camera.camera1.modules.BracketModule;
import com.troop.freedcam.camera.camera1.modules.IntervalModuleCamera1;
import com.troop.freedcam.camera.camera1.modules.PictureModule;
import com.troop.freedcam.camera.camera1.modules.PictureModuleMTK;
import com.troop.freedcam.camera.camera1.modules.VideoModule;
import com.troop.freedcam.camera.camera1.modules.VideoModuleG3;
import com.troop.freedcam.settings.Frameworks;
import com.troop.freedcam.settings.SettingsManager;
import com.troop.freedcam.utils.Log;

/**
 * Created by troop on 16.08.2014.
 */
public class ModuleHandler extends ModuleHandlerAbstract
{


    public  ModuleHandler (CameraControllerInterface cameraUiWrapper)
    {
        super(cameraUiWrapper);
    }

    @Override
    public void initModules()
    {
        //init the Modules DeviceDepending
        //splitting modules make the code foreach device cleaner
        String TAG = "cam.ModuleHandler";
        if (SettingsManager.getInstance().getFrameWork() == Frameworks.MTK)
        {
            Log.d(TAG, "load mtk picmodule");
            PictureModuleMTK thl5000 = new PictureModuleMTK(cameraUiWrapper,mBackgroundHandler,mainHandler);
            moduleList.put(thl5000.ModuleName(), thl5000);
        }
        else//else //use default pictureModule
        {
            Log.d(TAG, "load default picmodule");
            PictureModule pictureModule = new PictureModule(cameraUiWrapper,mBackgroundHandler,mainHandler);
            moduleList.put(pictureModule.ModuleName(), pictureModule);
            IntervalModuleCamera1 intervalModule = new IntervalModuleCamera1(cameraUiWrapper,mBackgroundHandler,mainHandler);
            moduleList.put(intervalModule.ModuleName(), intervalModule);
        }

        if (SettingsManager.getInstance().getFrameWork() == Frameworks.LG)
        {
            Log.d(TAG, "load lg videomodule");
            VideoModuleG3 videoModuleG3 = new VideoModuleG3(cameraUiWrapper,mBackgroundHandler,mainHandler);
            moduleList.put(videoModuleG3.ModuleName(), videoModuleG3);
        }
        else
        {
            Log.d(TAG, "load default videomodule");
            VideoModule videoModule = new VideoModule(cameraUiWrapper,mBackgroundHandler,mainHandler);
            moduleList.put(videoModule.ModuleName(), videoModule);
        }

        Log.d(TAG, "load hdr module");
        if (SettingsManager.getInstance().getFrameWork() != Frameworks.MTK)
        {
            BracketModule bracketModule = new BracketModule(cameraUiWrapper,mBackgroundHandler,mainHandler);
            moduleList.put(bracketModule.ModuleName(), bracketModule);
        }
    }

}