package com.example.ffmpegsdlplayer.activity;

import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.ffmpegsdlplayer.media.opgl.YUVRender;

/**
 * Created by h26376 on 2018/4/16.
 */

public class GLRenderer {
    private final String TAG = "GLRenderer";
    private Thread updateThread;
    private DecodeListener listener;
    private YUVRender yuvRender;
    private boolean updateSurface;
    private int video_width;
    private int video_height;
    private int rotate;
    private SurfaceTexture mSurfaceTexture;
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("yuv");
        System.loadLibrary("render");
    }

    public GLRenderer(SurfaceView surfaceView, final int mode) {
        Log.i(TAG, "GLRenderer :: GLRenderer");
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "SurfaceHolder :: surfaceCreated");
                yuvRender = new YUVRender(holder.getSurface(), mode);
                yuvRender.init(new YUVRender.RenderListener(){
                    @Override
                    public void OnInit(SurfaceTexture surfaceTexture) {
                        mSurfaceTexture = surfaceTexture;
                        if(updateThread == null){
                            listener.surfaceCreated();
                        }
                    }
                }, new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        updateSurface = true;
                        yuvRender.draw(surfaceTexture);
                    }
                });
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int screen_width, int screen_height) {
                Log.i(TAG, "SurfaceHolder :: surfaceChanged format="+format+" screen_width="+screen_width+" screen_height="+screen_height);
                yuvRender.setScreenSize(screen_width,screen_height);
                updateParameter(video_width,video_height,rotate);
                if(updateSurface) {
                    yuvRender.ReDraw();
                    yuvRender.ReDraw();
                }
                listener.surfaceChanged();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "SurfaceHolder :: surfaceDestroyed");
                yuvRender.destory();
                listener.surfaceDestroyed();
            }
        });
    }

//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        Log.i(TAG, "GLRenderer :: onSurfaceCreated");
//        if (!prog.isProgramBuilt()) {
//            prog.buildProgram();
//            Log.i(TAG, "GLRenderer :: buildProgram done");
//        }
//    }
//
//    @Override
//    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        Log.i(TAG, "GLRenderer :: onSurfaceChanged screen W x H : " + width + "x" + height);
//        screen_width = width;
//        screen_height = height;
//        updateZoom(render_zoom);
//        GLES20.glViewport(0, 0, screen_width, screen_height);
//    }
//
//
//    @Override
//    public void onDrawFrame(GL10 gl) {
//        synchronized (this) {
//            if (y != null) {
//                // reset position, have to be done
//                y.position(0);
//                u.position(0);
//                v.position(0);
//                prog.buildTextures(y, u, v, pixel_width, pixel_height);
//                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//                prog.drawFrame();
//            }
//        }
//    }


//    float[] multSquareVertices(float[] a, float[] b){
//        for(int i = 0; i<a.length; i++){
//            b[i] = a[i] * b[i];
//        }
//        return b;
//    }
    public void updateZoom(int zoom) {// 调整比例
        Log.i(TAG, "updateZoom: "+zoom);
        yuvRender.setZoom(zoom);
        if(updateSurface) {
            yuvRender.ReDraw();
        }
    }
    /**
     * this method will be called from native code, it happens when the video is about to play or
     * the video size changes.
     */
    public void updateParameter(int video_width, int video_height, int rotate) {
        this.video_width = video_width;
        this.video_height = video_height;
        this.rotate = rotate;
        Log.i(TAG, "updateParameter pixel W x H : "+ video_width + "x" + video_height + " rotate :" + rotate);
        switch (this.rotate) {
            case 0:
                yuvRender.setRotation(Surface.ROTATION_0,false,false);
                break;
            case 90:
                yuvRender.setRotation(Surface.ROTATION_90,false,false);
                break;
            case 180:
                yuvRender.setRotation(Surface.ROTATION_180,false,false);
                break;
            case 270:
                yuvRender.setRotation(Surface.ROTATION_270,false,false);
                break;
            default:
                yuvRender.setRotation(Surface.ROTATION_0,false,false);
                break;
        }
        yuvRender.setVideoSize(this.video_width, this.video_height);
    }

    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     */
    public void updateData(byte[] ydata, byte[] udata, byte[] vdata) {
//        Log.i(TAG, "updateData y : "+ ydata.length +" u : "+ udata.length +" v : "+ vdata.length);
        synchronized (this) {
            updateSurface = true;
            yuvRender.draw(ydata, udata, vdata);
        }
    }

    public void startRender(final boolean isYUV, final String url, final int wdith, final int height, final int pixel_type , final int fps, final boolean isStreamMedia) {
        Log.i(TAG, "startRender");
        if(updateThread == null){
            updateThread = new Thread(){
                @Override
                public void run() {
                    int status;
                    if(isYUV) {
                        status = nativeInitSDLThreadYUV(url, wdith, height, pixel_type, fps);
                    }
                    else{
                        status = nativeInitSDLThread(url, isStreamMedia, 0, new Surface(mSurfaceTexture));
                    }
                    if(listener != null) {
                        listener.DecodeThreadFinish(status);
                    }
                }
            };
            updateThread.start();
        }
    }

    public void stopRender() {
        Log.i(TAG, "stopRender");
        if(updateThread != null){
            nativeBackSDLThread();
            try {
                updateThread.join();
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            updateThread = null;
        }
    }


    public void setListener(DecodeListener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    public void setProgressRate(int frameConut){
        if(listener != null) {
            listener.setProgressRate(frameConut);
        }
    }

    public void setProgressRateFull(){
        if(listener != null) {
            listener.setProgressRateFull();
        }
    }

    public void setProgressDuration(long duration){
        if(listener != null) {
            listener.setProgressDuration(duration);
        }
    }

    public void setProgressDTS(long dts){
        if(listener != null) {
            listener.setProgressDTS(dts);
        }
    }

    public void showIFrameDTS(long I_Frame_dts,int forwardOffset){
        if(listener != null) {
            listener.showIFrameDTS(I_Frame_dts,forwardOffset);
        }
    }

    public void initOrientation(){
        if(listener != null) {
            listener.initOrientation();
        }
    }

    public void hideLoading(){
        if(listener != null) {
            listener.hideLoading();
        }
    }

    public void showLoading(){
        if(listener != null) {
            listener.showLoading();
        }
    }

    public void changeCodec(String codec_name){
        if(listener != null) {
            listener.changeCodec(codec_name);
        }
    }

    public void setTimeBase(double timeBase){
        if(listener != null) {
            listener.setTimeBase(timeBase);
        }
    }

    interface DecodeListener {
        void setProgressRate(int frameConut);
        void setProgressRateFull();
        void setProgressDuration(long duration);
        void setProgressDTS(long dts);
        void showIFrameDTS(long I_Frame_dts,int forwardOffset);
        void initOrientation();
        void hideLoading();
        void showLoading();
        void changeCodec(String codec_name);
        void setTimeBase(double timeBase);
        void DecodeThreadFinish(int result_code);
        void surfaceCreated();
        void surfaceChanged();
        void surfaceDestroyed();
    }

    //CUSTOM JNI
    private native int nativeInitSDLThreadYUV(String url, int wdith, int height, int pixel_type, int fps);
    private native int nativeInitSDLThread(String url, boolean isStreamMedia, int rotate, Surface surface);
    private native void nativeBackSDLThread();
    public native void nativeCodecType(int codec_type);
    public native void nativePauseSDLThread();
    public native void nativePlaySDLThread();
    public native void nativeZoomSDLThread(int zoom);
    public native void nativeBackwardSDLThread(long skipFrame);
    public native void nativeForwardSDLThread(long skipFrame);
    public native void nativeSeekSDLThread(long seekFrame);
    public native void nativeUpdateSdlRect(int wdith,int height);
}

