package com.troop.freedcam.camera.basecamera.record;

import android.hardware.Camera;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.troop.freedcam.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.troop.freedcam.utils.ContextApplication;
import com.troop.freedcam.camera.basecamera.CameraControllerInterface;
import freed.cam.ui.themesample.handler.UserMessageHandler;
import com.troop.freedcam.file.holder.BaseHolder;
import com.troop.freedcam.settings.SettingKeys;
import com.troop.freedcam.settings.SettingsManager;

import com.troop.freedcam.file.holder.FileHolder;
import com.troop.freedcam.file.holder.UriHolder;
import com.troop.freedcam.utils.Log;
import com.troop.freedcam.utils.VideoMediaProfile;

/**
 * Created by KillerInk on 22.02.2018.
 */

public class VideoRecorder {
    private final MediaRecorder mediaRecorder;
    private final String TAG = VideoRecorder.class.getSimpleName();
    private MediaRecorder.OnErrorListener errorListener;
    private MediaRecorder.OnInfoListener infoListener;
    private Location location;

    private VideoMediaProfile currentVideoProfile;
    //MediaRecorder.VideoSource.SURFACE=2/CAMERA=1/DEFAULT=0
    private int outputSource;
    private File recordingFile;
    private int orientation;

    private Camera camera;

    CameraControllerInterface cameraControllerInterface;
    private Surface previewSurface;

    public VideoRecorder(CameraControllerInterface cameraControllerInterface, MediaRecorder recorder)
    {
        mediaRecorder = recorder;
        this.cameraControllerInterface = cameraControllerInterface;
    }

    public void setErrorListener(MediaRecorder.OnErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public void setInfoListener(MediaRecorder.OnInfoListener infoListener) {
        this.infoListener = infoListener;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setCurrentVideoProfile(VideoMediaProfile currentVideoProfile) {
        this.currentVideoProfile = currentVideoProfile;
    }

    public void setVideoSource(int outputSource) {
        this.outputSource = outputSource;
    }

    public void setRecordingFile(File recordingFile) {
        this.recordingFile = recordingFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Surface getSurface()
    {
        return mediaRecorder.getSurface();
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setPreviewSurface(Surface previewSurface)
    {
        this.previewSurface = previewSurface;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void start()
    {
        mediaRecorder.start();
    }

    public void stop()
    {
        mediaRecorder.stop();
    }

    public boolean prepare()
    {
        mediaRecorder.reset();
        if (camera != null)
            mediaRecorder.setCamera(camera);
        if (previewSurface != null)
            mediaRecorder.setPreviewDisplay(previewSurface);
        try {
            if (this.currentVideoProfile.maxRecordingSize != 0)
                mediaRecorder.setMaxFileSize(this.currentVideoProfile.maxRecordingSize);
        }
        catch (NullPointerException ex)
        {
            Log.WriteEx(ex);
            //return false;
        }
        try {
            if (this.currentVideoProfile.duration != 0)
                mediaRecorder.setMaxDuration(this.currentVideoProfile.duration);
        }
        catch (RuntimeException ex)
        {
            Log.WriteEx(ex);
            Log.e(TAG,"Failed to set Duration");
            //return false;
        }

        if (errorListener != null)
            mediaRecorder.setOnErrorListener(errorListener);

        if (infoListener != null)
            mediaRecorder.setOnInfoListener(infoListener);

        if (location != null)
            mediaRecorder.setLocation((float)location.getLatitude(),(float)location.getLongitude());

        mediaRecorder.setOrientationHint(orientation);

        switch (this.currentVideoProfile.Mode)
        {

            case Normal:
            case Highspeed:
                if (this.currentVideoProfile.isAudioActive)
                {
                    try {
                        mediaRecorder.setAudioSource(getAudioSource());
                        //mediaRecorder.setAudioEncoder(currentVideoProfile.audioCodec);
                    }
                    catch (IllegalArgumentException ex)
                    {
                        mediaRecorder.reset();
                        UserMessageHandler.sendMSG("AudioSource not Supported",true);
                        return false;
                    }
                    catch (IllegalStateException ex)
                    {
                        mediaRecorder.reset();
                        UserMessageHandler.sendMSG("AudioSource not Supported",true);
                        return false;
                    }
                }
                break;
            case Timelapse:
                break;
        }

        mediaRecorder.setVideoSource(outputSource);

        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        setRecorderFilePath();

        mediaRecorder.setVideoEncodingBitRate(this.currentVideoProfile.videoBitRate);
        mediaRecorder.setVideoFrameRate(this.currentVideoProfile.videoFrameRate);

         /*setCaptureRate

        Added in API level 11
        void setCaptureRate (double fps)
        Set video frame capture rate. This can be used to set a different video frame capture rate than the recorded video's playback rate.
        !!!!!! This method also sets the recording mode to time lapse.!!!!!
        In time lapse video recording, only video is recorded.
        Audio related parameters are ignored when a time lapse recording session starts, if an application sets them.*/
        //mediaRecorder.setCaptureRate((double)currentVideoProfile.videoFrameRate);

        mediaRecorder.setVideoSize(this.currentVideoProfile.videoFrameWidth, this.currentVideoProfile.videoFrameHeight);
        try {
            mediaRecorder.setVideoEncoder(this.currentVideoProfile.videoCodec);
        }
        catch (IllegalArgumentException ex)
        {
            mediaRecorder.reset();
            UserMessageHandler.sendMSG("VideoCodec not Supported",false);
        }

        switch (this.currentVideoProfile.Mode)
        {
            case Normal:
            case Highspeed:
                if (this.currentVideoProfile.isAudioActive)
                {
                    try {
                        mediaRecorder.setAudioEncoder(this.currentVideoProfile.audioCodec);
                    }
                    catch (IllegalArgumentException ex)
                    {
                        mediaRecorder.reset();
                        UserMessageHandler.sendMSG("AudioCodec not Supported",false);
                        return false;
                    }
                    mediaRecorder.setAudioChannels(this.currentVideoProfile.audioChannels);
                    mediaRecorder.setAudioEncodingBitRate(this.currentVideoProfile.audioBitRate);
                    mediaRecorder.setAudioSamplingRate(this.currentVideoProfile.audioSampleRate);
                }
                break;
            case Timelapse:
                float frame = 30;
                if (!TextUtils.isEmpty(SettingsManager.get(SettingKeys.TIMELAPSE_FRAMES).get()))
                    frame = Float.parseFloat(SettingsManager.get(SettingKeys.TIMELAPSE_FRAMES).get().replace(",", "."));
                else
                    SettingsManager.get(SettingKeys.TIMELAPSE_FRAMES).set(String.valueOf(frame));
                mediaRecorder.setCaptureRate(frame);
                break;
        }
        try {
            mediaRecorder.prepare();
        } catch (IOException ex) {
            Log.WriteEx(ex);
            UserMessageHandler.sendMSG("Prepare failed :" + ex.getMessage(),false);
            return false;
        }
        return true;
    }

    private void setRecorderFilePath() {
        BaseHolder baseHolder = cameraControllerInterface.getActivityInterface().getFileListController().getNewMovieFileHolder(recordingFile, SettingsManager.getInstance().GetWriteExternal(),SettingsManager.getInstance().GetBaseFolder());
        try {
            setToMediaRecorder(mediaRecorder,baseHolder);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setToMediaRecorder(MediaRecorder recorder, BaseHolder baseHolder) throws FileNotFoundException {
        if (baseHolder instanceof FileHolder)
        {
            recorder.setOutputFile(((FileHolder)baseHolder).getFile().getAbsolutePath());
        }
        else if (baseHolder instanceof UriHolder)
        {
            ParcelFileDescriptor fileDescriptor = ((UriHolder)baseHolder).getParcelFileDescriptor();
            recorder.setOutputFile(fileDescriptor.getFileDescriptor());
        }
    }

    public void release()
    {
        mediaRecorder.release();
    }

    public void reset(){
        mediaRecorder.reset();
    }

    public int getAudioSource()
    {
        String as = SettingsManager.get(SettingKeys.VIDEO_AUDIO_SOURCE).get();
        if (as.equals(ContextApplication.getStringFromRessources(R.string.video_audio_source_mic)))
            return MediaRecorder.AudioSource.MIC;
        if (as.equals(ContextApplication.getStringFromRessources(R.string.video_audio_source_camcorder)))
            return MediaRecorder.AudioSource.CAMCORDER;
        if (as.equals(ContextApplication.getStringFromRessources(R.string.video_audio_source_voice_recognition)))
            return MediaRecorder.AudioSource.VOICE_RECOGNITION;
        if (as.equals(ContextApplication.getStringFromRessources(R.string.video_audio_source_voice_communication)))
            return MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        if (as.equals(ContextApplication.getStringFromRessources(R.string.video_audio_source_unprocessed)))
            return MediaRecorder.AudioSource.UNPROCESSED;
        return MediaRecorder.AudioSource.DEFAULT;
    }

}