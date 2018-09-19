/**
 * 
 */
package com.example.ffmpegsdlplayer.media.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.util.Log;

/**
 * OpenGL EGL 窗口默认工厂
 * 
 * @author chenyang
 *
 */
public class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
	/**
	 * 标签
	 */
	public static final String TAG = DefaultWindowSurfaceFactory.class.getSimpleName();

	@Override
	public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
		EGLSurface result = null;
		try {
			result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
		} catch (IllegalArgumentException e) {
			// This exception indicates that the surface flinger surface
			// is not valid. This can happen if the surface flinger surface has
			// been torn down, but the application has not yet been
			// notified via SurfaceHolder.Callback.surfaceDestroyed.
			// In theory the application should be notified first,
			// but in practice sometimes it is not. See b/4588890
			Log.e(TAG, "eglCreateWindowSurface", e);
		}
		return result;
	}

	@Override
	public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
		egl.eglDestroySurface(display, surface);
	}
}
