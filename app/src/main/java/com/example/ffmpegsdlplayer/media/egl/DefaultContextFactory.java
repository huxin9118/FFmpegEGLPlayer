/**
 * 
 */
package com.example.ffmpegsdlplayer.media.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import android.util.Log;

/**
 * OpenGL EGL 上下文默认工厂
 * @author chenyang
 *
 */
public class DefaultContextFactory implements EGLContextFactory {
	/**
	 * 标签
	 */
	public static final String TAG = DefaultContextFactory.class.getSimpleName();

	private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

	/**
	 * OpenGL版本
	 */
	private int mEGLContextClientVersion;

	/**
	 * 构造方法
	 * 
	 * @param eglContextClientVersion
	 * @see #mEGLContextClientVersion
	 */
	public DefaultContextFactory(int eglContextClientVersion) {
		super();
		this.mEGLContextClientVersion = eglContextClientVersion;
	}


	public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
		int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion, EGL10.EGL_NONE };

		return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, mEGLContextClientVersion != 0 ? attrib_list
				: null);
	}

	public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
		if (!egl.eglDestroyContext(display, context)) {
			Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
		}
	}
}
