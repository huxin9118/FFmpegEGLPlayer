/**
 * 
 */
package com.example.ffmpegsdlplayer.media.opgl;

import java.util.concurrent.ArrayBlockingQueue;


import android.util.Log;
import android.view.Surface;

import com.example.ffmpegsdlplayer.media.opgl.egl.DefaultContextFactory;
import com.example.ffmpegsdlplayer.media.opgl.egl.DefaultWindowSurfaceFactory;
import com.example.ffmpegsdlplayer.media.opgl.egl.EGLConfigChooser;
import com.example.ffmpegsdlplayer.media.opgl.egl.EGLContextFactory;
import com.example.ffmpegsdlplayer.media.opgl.egl.EGLHelper;
import com.example.ffmpegsdlplayer.media.opgl.egl.EGLWindowSurfaceFactory;
import com.example.ffmpegsdlplayer.media.opgl.egl.RGB565EGLConfigChooser;

/**
 * @author c22188
 *
 */
public class YUVRender {

	/**
	 * Android标签
	 */
	private static final String TAG = "YUVRender";

	/**
	 * OpenGL版本
	 */
	private static final int EGL_CONTEXT_CLIENT_VERSION = 2;
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
	 * EGL接口帮助类
	 */
	private EGLHelper mEGLHelper;
	/**
	 * OpenGL接口帮助类
	 */
	private OpenGLHelper mOpenGLHelper;

	private Surface mSurface;

	private int videoWidth;
	private int videoHeight;
	private int outputWidth;
	private int outputHeight;
	private byte[]yuv;

	private ArrayBlockingQueue<byte[]> mQueue = new ArrayBlockingQueue<>(10);

	private Thread yuvRenderThread = new Thread() {
		public void run() {
			initEGL();
			initOpenGL();
			while (yuvRenderThread == Thread.currentThread()) {
				try {
					byte[] yuv = mQueue.take();
					mOpenGLHelper.onVideoSizeChanged(videoWidth, videoHeight);
					mOpenGLHelper.onOutputSizeChanged(outputWidth, outputHeight);

					mOpenGLHelper.onDrawFrame(yuv);
					int ret = mEGLHelper.swap();
//					Log.d(TAG, "draw swap ret " + ret);
				} catch (InterruptedException e) {
					break;
				}
			}
			destoryEGL();
			destoryOpenGL();
		}
	};

	/**
	 * 构造方法
	 */
	public YUVRender(Surface surfaceHolder) {
		mSurface = surfaceHolder;
	}

	public void init(){
		mEGLConfigChooser = new RGB565EGLConfigChooser(true, EGL_CONTEXT_CLIENT_VERSION);
		mEGLContextFactory = new DefaultContextFactory(EGL_CONTEXT_CLIENT_VERSION);
		mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
		mEGLHelper = new EGLHelper(mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory, mSurface);
		mOpenGLHelper = new OpenGLHelper();
		yuvRenderThread.start();
	}

	public void draw(byte[] yuv) {
		this.yuv = yuv;
		mQueue.offer(this.yuv);
	}

	public void draw(byte[] y, byte[] u, byte[] v) {
		this.yuv = new byte[y.length+u.length+y.length];
		System.arraycopy(y, 0, this.yuv, 0 , y.length);
		System.arraycopy(u, 0, this.yuv, y.length , u.length);
		System.arraycopy(v, 0, this.yuv, y.length+u.length , v.length);
		mQueue.offer(this.yuv);
	}

	public void ReDraw() {
		if(this.yuv != null) {
			mQueue.offer(this.yuv);
		}
	}

	public void setVideoSize(int width, int height) {
		videoWidth = width;
		videoHeight = height;
	}

	public void setScreenSize(int width, int height) {
		outputWidth = width;
		outputHeight = height;
	}

	public void destory() {
		Thread t = yuvRenderThread;
		yuvRenderThread = null;
		if(t!=null){
			t.interrupt();
		}
	}

	/**
	 * 初始化EGL接口
	 */
	private void initEGL() {
		mEGLHelper.start();
		if (!mEGLHelper.createSurface()) {
			Log.w(TAG, "EGL Create Surface failure");
		}
	}

	/**
	 * 初始化OpenGL接口
	 */
	private void initOpenGL() {
		mOpenGLHelper.init(OpenGLHelper.NO_FILTER_VERTEX_SHADER, OpenGLHelper.NO_FILTER_FRAGMENT_SHADER);
	}

	public void setRotation(final int rotation, final boolean flipHorizontal, final boolean flipVertical) {
		mOpenGLHelper.setRotation(rotation,flipHorizontal,flipVertical);
	}

	public void setZoom(final int zoom) {
		mOpenGLHelper.setZoom(zoom);
	}

	/**
	 * 销毁EGL接口
	 */
	private void destoryEGL() {
		mEGLHelper.destroySurface();
		mEGLHelper.finish();
		mEGLHelper = null;
	}

	/**
	 * 销毁EGL接口
	 */
	private void destoryOpenGL() {
		mOpenGLHelper.destory();
		mOpenGLHelper = null;
	}

}
