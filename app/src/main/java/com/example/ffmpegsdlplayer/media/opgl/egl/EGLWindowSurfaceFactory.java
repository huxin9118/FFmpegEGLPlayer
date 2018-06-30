/**
 * 
 */
package com.example.ffmpegsdlplayer.media.opgl.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * OpenGL EGL 窗口工厂
 * 
 * @author chenyang
 *
 */
public interface EGLWindowSurfaceFactory {

	/**
	 * 创建EGL窗口
	 * 
	 * @param egl
	 *            EGL对象
	 * @param display
	 *            EGL显示对象
	 * @param config
	 *            EGL配置
	 * @param nativeWindow
	 *            本地窗口
	 * @return null if the surface cannot be constructed.
	 */
	EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow);

	/**
	 * 销毁EGL窗口
	 * @param egl
	 *            EGL对象
	 * @param display
	 *            EGL显示对象
	 * @param surface EGL窗口
	 */
	void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface);
}
