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

package com.troop.freedcam.camera.basecamera;

import android.graphics.Rect;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


import com.troop.freedcam.eventbus.events.FocusCoordinatesEvent;
import com.troop.freedcam.eventbus.updater.TouchToFocusUpdater;
import com.troop.freedcam.utils.Log;

/**
 * Created by troop on 09.12.2014.
 */
public abstract class AbstractFocusHandler
{
    private final String TAG = AbstractFocusHandler.class.getSimpleName();
    protected CameraControllerInterface cameraUiWrapper;


    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onFocusCoordinates(FocusCoordinatesEvent focusCoordinates)
    {
        startTouchFocus(focusCoordinates);
    }

    protected abstract void startTouchFocus(FocusCoordinatesEvent obj);

    protected AbstractFocusHandler(CameraControllerInterface cameraUiWrapper)
    {
        this.cameraUiWrapper = cameraUiWrapper;
    }

    public void StartTouchToFocus(int x1, int y1,int width1, int height1)
    {
        FocusCoordinatesEvent focusCoordinates = new FocusCoordinatesEvent();
        focusCoordinates.x = x1;
        focusCoordinates.y = y1;
        focusCoordinates.width = width1;
        focusCoordinates.height = height1;
        TouchToFocusUpdater.updateTouchToFocusCoordinates(focusCoordinates);
        //backgroundHandler.sendMessage(backgroundHandler.obtainMessage(MSG_SET_TOUCHTOFOCUS,focusCoordinates));
    }

    public abstract void SetMeteringAreas(int x, int y, int width, int height);
    public FocusHandlerInterface focusEvent;
    public abstract boolean isAeMeteringSupported();
    public abstract boolean isTouchSupported();

    protected void logFocusRect(Rect rect)
    {
        Log.d(TAG, "left:" + rect.left + "top:" + rect.top + "right:" + rect.right + "bottom:" + rect.bottom);
    }

    protected void logRect(Rect rect)
    {
        Log.d(TAG, "left:" + rect.left + "top:" + rect.top + "right:" + rect.right + "bottom:" + rect.bottom);
    }
}