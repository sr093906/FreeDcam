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

package com.troop.freedcam.camera.basecamera.cameraholder;

import android.location.Location;

import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import com.troop.freedcam.camera.basecamera.focus.FocusEvents;

/**
 * Created by troop on 12.12.2014.
 * holds the instance for the camera to work with
 */
public abstract class CameraHolderAbstract<C extends CameraControllerInterface> implements CameraHolderInterface
{
    protected C cameraUiWrapper;

    /**
     *
     * @param cameraUiWrapper to listen on camera state changes
     */
    protected CameraHolderAbstract(C cameraUiWrapper)
    {
        this.cameraUiWrapper = cameraUiWrapper;
    }

    @Override
    public abstract boolean OpenCamera(int camera);

    @Override
    public abstract void CloseCamera();


}
