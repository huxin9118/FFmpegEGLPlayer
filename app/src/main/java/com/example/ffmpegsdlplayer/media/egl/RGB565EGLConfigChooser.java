/**
 * 
 */
package com.example.ffmpegsdlplayer.media.egl;

/**
 * This class will choose a RGB_565 surface with
 * or without a depth buffer.
 * 
 * @author chenyang
 *
 */
public class RGB565EGLConfigChooser extends ComponentSizeChooser {
	/**
	 * 标签
	 */
	public static final String TAG = RGB565EGLConfigChooser.class.getSimpleName();

	/**
	 * 构造方法
	 * 
	 * @param withDepthBuffer
	 * @param eglContextClientVersion
	 */
	public RGB565EGLConfigChooser(boolean withDepthBuffer, int eglContextClientVersion) {
		super(5, 6, 5, 0, withDepthBuffer ? 16 : 0, 0, eglContextClientVersion);
	}

}
