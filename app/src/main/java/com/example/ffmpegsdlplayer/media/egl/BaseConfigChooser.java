/**
 * 
 */
package com.example.ffmpegsdlplayer.media.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * OpenGL EGL 基础配置选择器
 * 
 * @author chenyang
 *
 */
public abstract class BaseConfigChooser implements EGLConfigChooser {
	/**
	 * 标签
	 */
	public static final String TAG = BaseConfigChooser.class.getSimpleName();

	/**
	 * 配置参数
	 */
	protected int[] mConfigSpec;

	/**
	 * OpenGL版本
	 */
	private int mEGLContextClientVersion;

	/**
	 * 构造方法
	 * 
	 * @param configSpec
	 *            配置参数
	 * @param eglContextClientVersion
	 *            OpenGL版本
	 */
	public BaseConfigChooser(int[] configSpec, int eglContextClientVersion) {
		mConfigSpec = filterConfigSpec(configSpec);
		mEGLContextClientVersion = eglContextClientVersion;
	}


	@Override
	public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
		int[] num_config = new int[1];
		if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config)) {
			throw new IllegalArgumentException("eglChooseConfig failed");
		}

		int numConfigs = num_config[0];

		if (numConfigs <= 0) {
			throw new IllegalArgumentException("No configs match configSpec");
		}

		EGLConfig[] configs = new EGLConfig[numConfigs];
		if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs, num_config)) {
			throw new IllegalArgumentException("eglChooseConfig#2 failed");
		}
		EGLConfig config = chooseConfig(egl, display, configs);
		if (config == null) {
			throw new IllegalArgumentException("No config chosen");
		}
		return config;
	}

	/**
	 * 过滤配置参数
	 * 
	 * @param configSpec
	 *            配置参数
	 * @return 配置参数
	 */
	private int[] filterConfigSpec(int[] configSpec) {
		if (mEGLContextClientVersion != 2) {
			return configSpec;
		}
		/*
		 * We know none of the subclasses define EGL_RENDERABLE_TYPE.
		 * And we know the configSpec is well formed.
		 */
		int len = configSpec.length;
		int[] newConfigSpec = new int[len + 2];
		System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
		newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
		newConfigSpec[len] = 4; /* EGL_OPENGL_ES2_BIT */
		newConfigSpec[len + 1] = 0x3033;
		newConfigSpec[len + 2] = EGL10.EGL_NONE;
		return newConfigSpec;
	}

	/**
	 * @see BaseConfigChooser#chooseConfig(EGL10, EGLDisplay)
	 * @param egl
	 *            EGL对象
	 * @param display
	 *            EGL显示对象
	 * @param configs
	 *            EGL配置
	 * @return EGL配置
	 */
	abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs);
}
