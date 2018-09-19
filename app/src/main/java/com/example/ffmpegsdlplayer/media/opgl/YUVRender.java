/**
 * 
 */
package com.example.ffmpegsdlplayer.media.opgl;

import java.util.concurrent.ArrayBlockingQueue;


import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.example.ffmpegsdlplayer.media.egl.DefaultContextFactory;
import com.example.ffmpegsdlplayer.media.egl.DefaultWindowSurfaceFactory;
import com.example.ffmpegsdlplayer.media.egl.EGLConfigChooser;
import com.example.ffmpegsdlplayer.media.egl.EGLContextFactory;
import com.example.ffmpegsdlplayer.media.egl.EGLHelper;
import com.example.ffmpegsdlplayer.media.egl.EGLWindowSurfaceFactory;
import com.example.ffmpegsdlplayer.media.egl.RGB565EGLConfigChooser;

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
	private int renderMode;

	private int videoWidth;
	private int videoHeight;
	private int outputWidth;
	private int outputHeight;
	private Object data;

	private ArrayBlockingQueue<Object> mQueue = new ArrayBlockingQueue<>(10);

	private Thread yuvRenderThread;
	private boolean isRunning;
	private SurfaceTexture surfaceTexture;
	SurfaceTexture.OnFrameAvailableListener surfaceTextureListener;
	RenderListener renderThreadListener;
	/**
	 * 构造方法
	 */
	public YUVRender(Surface surface ,final int mode) {
		mSurface = surface;
		renderMode = mode;
	}

	public void init(YUVRender.RenderListener renderListener,SurfaceTexture.OnFrameAvailableListener frameAvailableListener){
		renderThreadListener = renderListener;
		surfaceTextureListener = frameAvailableListener;
		mEGLConfigChooser = new RGB565EGLConfigChooser(true, EGL_CONTEXT_CLIENT_VERSION);
		mEGLContextFactory = new DefaultContextFactory(EGL_CONTEXT_CLIENT_VERSION);
		mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
		mEGLHelper = new EGLHelper(mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory, mSurface);
		if(renderMode == 0){
			mOpenGLHelper = new YUVOpenGLHelper();
		}else if(renderMode == 1){
			mOpenGLHelper = new TextureOpenGLHelper();
		}
		yuvRenderThread = new RenderThread();
		yuvRenderThread.start();
	}

	public int getTextureId(){
		if(mOpenGLHelper != null) {
			return mOpenGLHelper.getTextureId();
		}
		return -1;
	}

	public interface RenderListener{
		void OnInit(SurfaceTexture surfaceTexture);
	}

	class RenderThread extends Thread{
		public void run() {
			initEGL();
			initOpenGL();
//			int textures[] = new int[1];
//			GLES20.glGenTextures(1, textures, 0);
//			mTextureId = textures[0];
			surfaceTexture = new SurfaceTexture(mOpenGLHelper.getTextureId());
			surfaceTexture.setOnFrameAvailableListener(surfaceTextureListener);
			isRunning = true;
			renderThreadListener.OnInit(surfaceTexture);
			while (isRunning) {
				try {
					Object data = mQueue.take();
					if(isRunning) {
						mOpenGLHelper.onVideoSizeChanged(videoWidth, videoHeight);
						mOpenGLHelper.onOutputSizeChanged(outputWidth, outputHeight);
						Log.i(TAG, "run: mQueue.size()="+mQueue.size());
						mOpenGLHelper.onDrawFrame(data);
						int ret = mEGLHelper.swap();
//					Log.d(TAG, "draw swap ret " + ret);
					}
				} catch (InterruptedException e) {
					break;
				}
			}
			destoryEGL();
			destoryOpenGL();
		}
	}

	public void draw(byte[] yuv) {
		data = yuv;
		mQueue.offer(data);
	}

	public void draw(byte[] y, byte[] u, byte[] v) {
		data = new byte[y.length+u.length+y.length];
		System.arraycopy(y, 0, data, 0 , y.length);
		System.arraycopy(u, 0, data, y.length , u.length);
		System.arraycopy(v, 0, data, y.length+u.length , v.length);
		mQueue.offer(data);
	}

	public void draw(SurfaceTexture surfaceTexture) {
		data = surfaceTexture;
		mQueue.offer(surfaceTexture);
	}

	public void ReDraw() {
		if(data != null) {
			mQueue.offer(data);
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
		isRunning = false;
		mQueue.offer(new Object());
		try {
			yuvRenderThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		if(renderMode == 0) {
			mOpenGLHelper.init(YUVOpenGLHelper.NO_FILTER_VERTEX_SHADER, YUVOpenGLHelper.NO_FILTER_FRAGMENT_SHADER);
		}else if(renderMode == 1) {
			mOpenGLHelper.init(TextureOpenGLHelper.VERTEX_SHADER, TextureOpenGLHelper.FRAGMENT_SHADER);
		}
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
