/**
 * 
 */
package com.example.ffmpegsdlplayer.media.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * OpenGL EGL 配置选择器
 * 
 * @author chenyang
 *
 */
public interface EGLConfigChooser {
	/**
	 * Choose a configuration from the list. Implementors typically
	 * implement this method by calling {@link EGL10#eglChooseConfig} and
	 * iterating through the results. Please consult the
	 * EGL specification available from The Khronos Group to learn how to call
	 * eglChooseConfig.
	 * 
	 * @param egl
	 *            the EGL10 for the current display.
	 * @param display
	 *            the current display.
	 * @return the chosen configuration.
	 */
	EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
}
