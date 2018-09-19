/**
 * 
 */
package com.example.ffmpegsdlplayer.media.egl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

import android.util.Log;
import android.view.Surface;

/**
 * EGL接口帮助类
 * 
 * @author chenyang
 *
 */
public class EGLHelper {
	/**
	 * 标签
	 */
	public static final String TAG = EGLHelper.class.getSimpleName();
	/**
	 * 是否开启调试日志
	 */
	private static final boolean DEBUG = true;

	private EGL10 mEgl;
	private EGLDisplay mEglDisplay;
	private EGLSurface mEglSurface;
	private EGLConfig mEglConfig;
	private EGLContext mEglContext;

	/**
	 * OpenGL EGL 配置选择器
	 */
	private EGLConfigChooser mEGLConfigChooser;
	/**
	 * OpenGL EGL 上下文工厂
	 */
	private EGLContextFactory mEGLContextFactory;
	/**
	 * OpenGL EGL 窗口工厂
	 */
	private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;

	/**
	 * 显示句柄
	 */
	private Surface surface;

	/**
	 * 构造方法
	 * 
	 * @param mEGLConfigChooser
	 * @param mEGLContextFactory
	 * @param mEGLWindowSurfaceFactory
	 * @param surface
	 *
	 */
	public EGLHelper(EGLConfigChooser mEGLConfigChooser, EGLContextFactory mEGLContextFactory,
			EGLWindowSurfaceFactory mEGLWindowSurfaceFactory, Surface surface) {
		super();
		this.mEGLConfigChooser = mEGLConfigChooser;
		this.mEGLContextFactory = mEGLContextFactory;
		this.mEGLWindowSurfaceFactory = mEGLWindowSurfaceFactory;
		this.surface = surface;
	}

	public void start() {
		if (DEBUG) {
			Log.w("EglHelper", "start() tid=" + Thread.currentThread().getId());
		}
		/*
		 * Get an EGL instance
		 */
		mEgl = (EGL10) EGLContext.getEGL();

		/*
		 * Get to the default display.
		 */
		mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
			throw new RuntimeException("eglGetDisplay failed");
		}

		/*
		 * We can now initialize EGL for that display
		 */
		int[] version = new int[2];
		if (!mEgl.eglInitialize(mEglDisplay, version)) {
			throw new RuntimeException("eglInitialize failed");
		}

		if (surface == null) {
			mEglConfig = null;
			mEglContext = null;
		} else {
			mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);

			/*
			 * Create an EGL context. We want to do this as rarely as we can,
			 * because an
			 * EGL context is a somewhat heavy object.
			 */
			mEglContext = mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);
		}
		if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
			mEglContext = null;
			throwEglException("createContext");
		}
		if (DEBUG) {
			Log.w("EglHelper", "createContext " + mEglContext + " tid=" + Thread.currentThread().getId());
		}

		mEglSurface = null;
	}

	/**
	 * Create an egl surface for the current SurfaceHolder surface. If a surface
	 * already exists, destroy it before creating the new surface.
	 *
	 * @return true if the surface was created successfully.
	 */
	public boolean createSurface() {
		if (DEBUG) {
			Log.w("EglHelper", "createSurface()  tid=" + Thread.currentThread().getId());
		}
		/*
		 * Check preconditions.
		 */
		if (mEgl == null) {
			throw new RuntimeException("egl not initialized");
		}
		if (mEglDisplay == null) {
			throw new RuntimeException("eglDisplay not initialized");
		}
		if (mEglConfig == null) {
			throw new RuntimeException("mEglConfig not initialized");
		}

		/*
		 * The window size has changed, so we need to create a new
		 * surface.
		 */
		destroySurfaceImp();

		if (surface != null) {
			mEglSurface = mEGLWindowSurfaceFactory.createWindowSurface(mEgl, mEglDisplay, mEglConfig, surface);
		} else {
			mEglSurface = null;
		}

		if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
			int error = mEgl.eglGetError();
			if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
				Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
			}
			return false;
		}

		/*
		 * Before we can issue GL commands, we need to make sure
		 * the context is current and bound to a surface.
		 */
		if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
			/*
			 * Could not make the context current, probably because the
			 * underlying
			 * SurfaceView surface has been destroyed.
			 */
			logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", mEgl.eglGetError());
			return false;
		}

		int[] query = new int[1];
		if (!mEgl.eglQuerySurface(mEglDisplay, mEglSurface, EGL10.EGL_WIDTH, query)
				|| !mEgl.eglQuerySurface(mEglDisplay, mEglSurface, EGL10.EGL_HEIGHT, query)) {
			Log.e("EglHelper", "eglQuerySurface EGL_WIDTH EGL_HEIGHT returned false.");
			return false;
		}
		
		return true;
	}

	/**
	 * Create a GL object for the current EGL context.
	 * 
	 * @return
	 */
	public GL createGL() {
		GL gl = mEglContext.getGL();
		return gl;
	}

	/**
	 * Display the current render surface.
	 * 
	 * @return the EGL error code from eglSwapBuffers.
	 */
	public int swap() {
		if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
			return mEgl.eglGetError();
		}
		return EGL10.EGL_SUCCESS;
	}

	public void destroySurface() {
		if (DEBUG) {
			Log.w("EglHelper", "destroySurface()  tid=" + Thread.currentThread().getId());
		}
		destroySurfaceImp();
	}

	private void destroySurfaceImp() {
		if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
			mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			if (surface != null) {
				mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
			}
			mEglSurface = null;
		}
	}

	public void finish() {
		if (DEBUG) {
			Log.w("EglHelper", "finish() tid=" + Thread.currentThread().getId());
		}
		if (mEglContext != null) {
			mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
			mEglContext = null;
		}
		if (mEglDisplay != null) {
			mEgl.eglTerminate(mEglDisplay);
			mEglDisplay = null;
		}
	}

	private void throwEglException(String function) {
		throwEglException(function, mEgl.eglGetError());
	}

	public static void throwEglException(String function, int error) {
		String message = formatEglError(function, error);
		if (DEBUG) {
			Log.e("EglHelper", "throwEglException tid=" + Thread.currentThread().getId() + " " + message);
		}
		throw new RuntimeException(message);
	}

	public static void logEglErrorAsWarning(String tag, String function, int error) {
		Log.w(tag, formatEglError(function, error));
	}

	public static String formatEglError(String function, int error) {
		return function + " failed: " + getErrorString(error);
	}

	/**
	 * 返回EGL错误原因
	 * 
	 * @param error
	 *            错误码
	 * @return 错误原因
	 */
	public static String getErrorString(int error) {
		switch (error) {
		case EGL11.EGL_SUCCESS:
			return "EGL_SUCCESS";
		case EGL11.EGL_NOT_INITIALIZED:
			return "EGL_NOT_INITIALIZED";
		case EGL11.EGL_BAD_ACCESS:
			return "EGL_BAD_ACCESS";
		case EGL11.EGL_BAD_ALLOC:
			return "EGL_BAD_ALLOC";
		case EGL11.EGL_BAD_ATTRIBUTE:
			return "EGL_BAD_ATTRIBUTE";
		case EGL11.EGL_BAD_CONFIG:
			return "EGL_BAD_CONFIG";
		case EGL11.EGL_BAD_CONTEXT:
			return "EGL_BAD_CONTEXT";
		case EGL11.EGL_BAD_CURRENT_SURFACE:
			return "EGL_BAD_CURRENT_SURFACE";
		case EGL11.EGL_BAD_DISPLAY:
			return "EGL_BAD_DISPLAY";
		case EGL11.EGL_BAD_MATCH:
			return "EGL_BAD_MATCH";
		case EGL11.EGL_BAD_NATIVE_PIXMAP:
			return "EGL_BAD_NATIVE_PIXMAP";
		case EGL11.EGL_BAD_NATIVE_WINDOW:
			return "EGL_BAD_NATIVE_WINDOW";
		case EGL11.EGL_BAD_PARAMETER:
			return "EGL_BAD_PARAMETER";
		case EGL11.EGL_BAD_SURFACE:
			return "EGL_BAD_SURFACE";
		case EGL11.EGL_CONTEXT_LOST:
			return "EGL_CONTEXT_LOST";
		default:
			return getHex(error);
		}
	}

	private static String getHex(int value) {
		return "0x" + Integer.toHexString(value);
	}
}
