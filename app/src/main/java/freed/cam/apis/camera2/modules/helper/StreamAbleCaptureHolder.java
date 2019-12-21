package freed.cam.apis.camera2.modules.helper;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;


import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import freed.ActivityInterface;
import freed.cam.apis.basecamera.modules.ModuleInterface;
import freed.cam.apis.basecamera.modules.WorkFinishEvents;
import freed.cam.ui.themesample.handler.UserMessageHandler;
import freed.utils.Log;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class StreamAbleCaptureHolder extends ImageCaptureHolder {

    private static final String TAG = StreamAbleCaptureHolder.class.getSimpleName();
    //the connection to the server
    private Socket socket;
    //used to send data to the target
    private BufferedOutputStream bufferedOutputStream;
    private final BlockingQueue<Image> imageBlockingQueue;
    private FileStreamRunner fileStreamRunner;
    private final byte FRAME_START_FLAG = (byte)0xff;

    // Settings for cropping the image
    private int mCropsize = 100;
    private int x_crop_pos = 0;
    private int y_crop_pos = 0;

    public void stop()
    {
        if (fileStreamRunner != null)
            fileStreamRunner.stop();
    }

    public void setCropsize(int myCropsize){
        this.mCropsize = myCropsize;
    }

    public StreamAbleCaptureHolder(CameraCharacteristics characteristicss, CaptureType captureType, ActivityInterface activitiy, ModuleInterface imageSaver, WorkFinishEvents finish, RdyToSaveImg rdyToSaveImg, Socket socket) {
        super(characteristicss, captureType, activitiy, imageSaver, finish, rdyToSaveImg);
        this.socket =socket;
        try {
            this.bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Log.WriteEx(e);
        }
        catch (NullPointerException e)
        {
            Log.WriteEx(e);
        }
        imageBlockingQueue = new LinkedBlockingQueue<>(4);
        if (bufferedOutputStream != null) {
            fileStreamRunner = new FileStreamRunner();
            Thread thread = new Thread(fileStreamRunner);
            thread.start();

        }
        else
            UserMessageHandler.sendMSG("Not Connected to Server ",false);
    }

   /* @Override
    public void onImageAvailable(ImageReader reader) {
        Log.d(TAG, "OnRawAvailible waiting: ");

        final Image image = reader.acquireLatestImage();
        if (image == null)
            return;
        if (image.getFormat() != ImageFormat.RAW_SENSOR)
            image.close();
        else {
            Log.d(TAG, "add image to Queue left:" + imageBlockingQueue.remainingCapacity());
            try {
                imageBlockingQueue.put(image);
            } catch (InterruptedException e) {
                Log.WriteEx(e);
            }
            rdyToSaveImg.onRdyToSaveImg(StreamAbleCaptureHolder.this);
        }

    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        SetCaptureResult(result);

    }*/

    @Override
    protected void saveImage(Image image, String f) {
        Log.d(TAG, "add image to Queue left:" + imageBlockingQueue.remainingCapacity());
        try {
            imageBlockingQueue.put(image);
        } catch (InterruptedException e) {
            Log.WriteEx(e);
        }
    }

    /**
     *
     * @param x_crop_pos start crop position in the pixel area
     * @param y_crop_pos start crop position int the pixel area
     * @param buf_width length of the buffer in pixel size
     * @param buf_height length of the buffer in pixel size
     * @param image to get cropped
     * @return
     */
    private byte[] cropByteArray(int x_crop_pos ,int y_crop_pos, int buf_width, int buf_height, Image image)
    {
        int x_center = image.getWidth() / 2 ;
        int y_center = image.getHeight() / 2 ;
        int x_offset = x_center - (buf_width/2);
        int x_end = (x_offset +buf_width);
        int y_offset =  y_center - (buf_height/2);
        int y_end = (y_offset +buf_height);
        int rawsize = buf_height*buf_width*2;
        byte bytes[] = new byte[buf_width*buf_height*2];
        int rowsize = image.getWidth()*2;
        int bytepos = 0;
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        for (int y = y_offset; y < y_end; y++) {
            for (int x = x_offset*2; x < x_end*2; x++) {
                bytes[bytepos++] = buffer.get(y*rowsize+x);
            }
        }
        if (buffer != null) {
            buffer.clear();
            buffer = null;
        }
        return bytes;
    }

    private class FileStreamRunner implements Runnable
    {
        boolean run = false;
        private int count = 0;
        public FileStreamRunner()
        {
            run = true;
            count = 0;
        }

        public void stop(){ run = false; }

        @Override
        public void run() {
            if (bufferedOutputStream == null)
                return;
            Image image = null;
            while (run) {
                try {
                    image = imageBlockingQueue.take();
                } catch (InterruptedException e) {
                    Log.WriteEx(e);
                }

                Log.d(TAG,"Send img " + count++);
                byte[] bytes = cropByteArray(x_crop_pos, y_crop_pos, mCropsize, mCropsize, image);
                image.close();
                image = null;
                try {
                    //sending plain bayer bytearray with simple start end of file
                    //bufferedOutputStream.write("START".getBytes());
                    bufferedOutputStream.write(FRAME_START_FLAG);
                    Log.d(TAG, "Send data : " + bytes.length);
                    bufferedOutputStream.write(bytes);
                    //bufferedOutputStream.write("END".getBytes());
                    bufferedOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                /*synchronized (StreamAbleCaptureHolder.class) {
                    StreamAbleCaptureHolder.class.notify();
                }*/
            }
        }
    }
}
