package com.example.ffmpegsdlplayer.media.opgl;

/**
 * Created by h26376 on 2018/7/2.
 */

public interface OpenGLHelper {
    void init(String strVSource, String strFSource);
    void destory();
    int getTextureId();
    void onDrawFrame(Object data);
    void onOutputSizeChanged(final int width, final int height);
    void onVideoSizeChanged(final int width, final int height);
    void setRotation(final int rotation, final boolean flipHorizontal, final boolean flipVertical);
    void setZoom(final int zoom);
}
