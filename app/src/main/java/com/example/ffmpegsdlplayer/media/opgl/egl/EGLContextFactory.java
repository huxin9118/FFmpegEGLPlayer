/**
 * 
 */
package com.example.ffmpegsdlplayer.media.opgl.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * OpenGL EGL 上下文工厂
 * @author chenyang
 *
 */
public interface EGLContextFactory {
	/**
	 * 创建上下文
	 * @param egl EGL对象
	 * @param display EGL显示对象
	 * @param eglConfig EGL配置
	 * @return EGL上下文
	 */
    EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig);
	/**
	 * 销毁上下文
	 * @param egl EGL对象
	 * @param display EGL显示对象
	 * @param context EGL上下文
	 */
    void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);
}
