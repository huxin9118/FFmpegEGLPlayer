/**
 * 
 */
package com.example.ffmpegsdlplayer.media.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Choose a configuration with exactly the specified r,g,b,a sizes,
 * and at least the specified depth and stencil sizes.
 * 
 * @author chenyang
 *
 */
public class ComponentSizeChooser extends BaseConfigChooser {
	/**
	 * 标签
	 */
	public static final String TAG = ComponentSizeChooser.class.getSimpleName();
	private int[] mValue;
	// Subclasses can adjust these values:
	protected int mRedSize;
	protected int mGreenSize;
	protected int mBlueSize;
	protected int mAlphaSize;
	protected int mDepthSize;
	protected int mStencilSize;

	/**
	 * 构造方法
	 * 
	 * @param redSize
	 *            RGB红色值存储大小
	 * @param greenSize
	 *            RGB绿色值存储大小
	 * @param blueSize
	 *            RGB蓝色值存储大小
	 * @param alphaSize
	 *            alpha值存储大小
	 * @param depthSize
	 *            深度值存储大小
	 * @param stencilSize
	 *            模板值存储大小
	 * @param eglContextClientVersion
	 *            OpenGL版本
	 */
	public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize,
			int stencilSize, int eglContextClientVersion) {
		super(new int[] { EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE, blueSize,
				EGL10.EGL_ALPHA_SIZE, alphaSize, EGL10.EGL_DEPTH_SIZE, depthSize, EGL10.EGL_STENCIL_SIZE, stencilSize,
				EGL10.EGL_NONE }, eglContextClientVersion);
		mValue = new int[1];
		mRedSize = redSize;
		mGreenSize = greenSize;
		mBlueSize = blueSize;
		mAlphaSize = alphaSize;
		mDepthSize = depthSize;
		mStencilSize = stencilSize;
	}

	@Override
	EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
		for (EGLConfig config : configs) {
			int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
			int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
			if ((d >= mDepthSize) && (s >= mStencilSize)) {
				int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
				int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
				int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
				int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
				if ((r == mRedSize) && (g == mGreenSize) && (b == mBlueSize) && (a == mAlphaSize)) {
					return config;
				}
			}
		}
		return null;
	}

	/**
	 * 查询EGL配置参数
	 * 
	 * @param egl
	 *            EGL对象
	 * @param display
	 *            EGL显示对象
	 * @param config
	 *            EGL配置
	 * @param attribute
	 *            参数变量
	 * @param defaultValue
	 *            参数默认值
	 * @return 参数值
	 */
	private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {

		if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
			return mValue[0];
		}
		return defaultValue;
	}
}
