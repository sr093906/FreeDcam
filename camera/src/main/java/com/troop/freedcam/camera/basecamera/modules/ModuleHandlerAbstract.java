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

package com.troop.freedcam.camera.basecamera.modules;

import android.os.Handler;

import com.troop.freedcam.camera.R;
import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.eventbus.updater.ModuleUpdater;
import com.troop.freedcam.utils.BackgroundHandlerThread;
import com.troop.freedcam.utils.ContextApplication;
import com.troop.freedcam.utils.Log;

import java.util.AbstractMap;
import java.util.HashMap;

/**
 * Created by troop on 09.12.2014.
 */
public abstract class ModuleHandlerAbstract implements ModuleHandlerInterface
{

    private final String TAG = ModuleHandlerAbstract.class.getSimpleName();
    public AbstractMap<String, ModuleInterface> moduleList;
    protected ModuleInterface currentModule;
    protected CameraControllerInterface cameraUiWrapper;

    private BackgroundHandlerThread backgroundHandlerThread;

    protected Handler mBackgroundHandler;
    protected Handler mainHandler;

    public ModuleHandlerAbstract(CameraControllerInterface cameraUiWrapper)
    {
        this.cameraUiWrapper = cameraUiWrapper;
        moduleList = new HashMap<>();
        backgroundHandlerThread = new BackgroundHandlerThread(TAG);
        backgroundHandlerThread.create();
        mBackgroundHandler = new Handler(backgroundHandlerThread.getThread().getLooper());
    }

    /**
     * Load the new module
     * @param name of the module to load
     */
    @Override
    public void setModule(String name) {
        if (currentModule !=null) {
            currentModule.DestroyModule();
            //currentModule.SetCaptureStateChangedListner(null);
            currentModule = null;
        }
        currentModule = moduleList.get(name);
        if(currentModule == null)
            currentModule = moduleList.get(ContextApplication.getStringFromRessources(R.string.module_picture));
        currentModule.InitModule();
        ModuleHasChanged(currentModule.ModuleName());
        //currentModule.SetCaptureStateChangedListner(workerListner);
        Log.d(TAG, "Set Module to " + name);
    }

    @Override
    public String getCurrentModuleName() {
        if (currentModule != null)
            return currentModule.ModuleName();
        else return ContextApplication.getStringFromRessources(R.string.module_picture);
    }

    @Override
    public ModuleInterface getCurrentModule() {
        if (currentModule != null)
            return currentModule;
        return null;
    }

    @Override
    public boolean startWork() {
        if (currentModule != null) {
            currentModule.DoWork();
            return true;
        }
        else
            return false;
    }

    @Override
    public void SetIsLowStorage(Boolean x) {
        if( currentModule != null )
            currentModule.IsLowStorage(x);
    }

    /**
     * Gets thrown when the module has changed
     * @param module the new module that gets loaded
     */
    public void ModuleHasChanged(final String module)
    {
        ModuleUpdater.sendModuleChanged(module);
    }



}
