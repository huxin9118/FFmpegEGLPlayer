package com.example.ffmpegsdlplayer.media;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;

@SuppressLint("NewApi")
public class MediaSurfaceHolder implements SurfaceHolder, SurfaceTextureListener {

	static private final String TAG = MediaSurfaceHolder.class.getSimpleName();
	static private final boolean DEBUG = false;

	final ArrayList<Callback> mCallbacks = new ArrayList<Callback>();
	final ReentrantLock mSurfaceLock = new ReentrantLock();
	View mView;
	Surface mSurface;
	SurfaceTexture mSurfaceTexture;

	final Rect mSurfaceFrame = new Rect();

	boolean mDrawingStopped = true;

	int mRequestedWidth = -1;
	int mRequestedHeight = -1;
	/*
	 * Set SurfaceView's format to 565 by default to maintain backward
	 * compatibility with applications assuming this format.
	 */
	int mRequestedFormat = PixelFormat.RGB_565;

	int mLeft = -1;
	int mTop = -1;
	int mWidth = -1;
	int mHeight = -1;

	boolean mIsCreating = false;
	Rect mTmpDirty;

	long mLastLockTime = 0;

	public MediaSurfaceHolder(Surface mSurface, View view) {
		super();
		this.mSurface = mSurface;
		this.mView = view;
	}

	public MediaSurfaceHolder(TextureView view) {
		super();
		this.mView = view;
		view.setSurfaceTextureListener(this);
	}

	public boolean isCreating() {
		return mIsCreating;
	}

	public void addCallback(Callback callback) {
		synchronized (mCallbacks) {
			// This is a linear search, but in practice we'll
			// have only a couple callbacks, so it doesn't matter.
			if (mCallbacks.contains(callback) == false) {
				mCallbacks.add(callback);
			}
		}
	}

	public void removeCallback(Callback callback) {
		synchronized (mCallbacks) {
			mCallbacks.remove(callback);
		}
	}

	public void setFixedSize(int width, int height) {
		if (mRequestedWidth != width || mRequestedHeight != height) {
			mRequestedWidth = width;
			mRequestedHeight = height;
			requestLayout();
		}
	}

	public void setSizeFromLayout() {
		if (mRequestedWidth != -1 || mRequestedHeight != -1) {
			mRequestedWidth = mRequestedHeight = -1;
			requestLayout();
		}
	}

	public void setFormat(int format) {

		// for backward compatibility reason, OPAQUE always
		// means 565 for SurfaceView
		if (format == PixelFormat.OPAQUE)
			format = PixelFormat.RGB_565;

		mRequestedFormat = format;
		// if (mWindow != null) {
		// updateWindow(false, false);
		// }
	}

	/**
	 * @deprecated setType is now ignored.
	 */
	@Deprecated
	public void setType(int type) {
	}

	public void setKeepScreenOn(boolean screenOn) {
		// Message msg = mHandler.obtainMessage(KEEP_SCREEN_ON_MSG);
		// msg.arg1 = screenOn ? 1 : 0;
		// mHandler.sendMessage(msg);
	}

	public Canvas lockCanvas() {
		return internalLockCanvas(null);
	}

	public Canvas lockCanvas(Rect dirty) {
		return internalLockCanvas(dirty);
	}

	private final Canvas internalLockCanvas(Rect dirty) {
		mSurfaceLock.lock();

		if (DEBUG)
			Log.i(TAG, "Locking canvas... stopped=" + mDrawingStopped);

		Canvas c = null;
		if (!mDrawingStopped) {
			if (dirty == null) {
				if (mTmpDirty == null) {
					mTmpDirty = new Rect();
				}
				mTmpDirty.set(mSurfaceFrame);
				dirty = mTmpDirty;
			}

			try {
				c = mSurface.lockCanvas(dirty);
			} catch (Exception e) {
				Log.e(TAG, "Exception locking surface", e);
			}
		}

		if (DEBUG)
			Log.i(TAG, "Returned canvas: " + c);
		if (c != null) {
			mLastLockTime = SystemClock.uptimeMillis();
			return c;
		}

		// If the Surface is not ready to be drawn, then return null,
		// but throttle calls to this function so it isn't called more
		// than every 100ms.
		long now = SystemClock.uptimeMillis();
		long nextTime = mLastLockTime + 100;
		if (nextTime > now) {
			try {
				Thread.sleep(nextTime - now);
			} catch (InterruptedException e) {
			}
			now = SystemClock.uptimeMillis();
		}
		mLastLockTime = now;
		mSurfaceLock.unlock();

		return null;
	}

	public void unlockCanvasAndPost(Canvas canvas) {
		mSurface.unlockCanvasAndPost(canvas);
		mSurfaceLock.unlock();
	}

	public Surface getSurface() {
		return mSurface;
	}

	public Rect getSurfaceFrame() {
		return mSurfaceFrame;
	}

	/**
	 * @see #mSurfaceTexture
	 * @return the mSurfaceTexture
	 */
	public SurfaceTexture getSurfaceTexture() {
		return mSurfaceTexture;
	}

	private void requestLayout() {
		mView.requestLayout();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see SurfaceTextureListener#onSurfaceTextureAvailable(SurfaceTexture,
	 *      int, int)
	 */
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		mSurface = new Surface(surface);
		mSurfaceTexture = surface;
		mIsCreating = true;

		synchronized (mCallbacks) {
			// This is a linear search, but in practice we'll
			// have only a couple callbacks, so it doesn't matter.
			for (Callback callback : mCallbacks) {
				callback.surfaceCreated(this);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see SurfaceTextureListener#onSurfaceTextureSizeChanged(SurfaceTexture,
	 *      int, int)
	 */
	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		mSurfaceTexture = surface;

		synchronized (mCallbacks) {
			// This is a linear search, but in practice we'll
			// have only a couple callbacks, so it doesn't matter.
			for (Callback callback : mCallbacks) {
				callback.surfaceChanged(this, mRequestedFormat, width, height);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see SurfaceTextureListener#onSurfaceTextureDestroyed(SurfaceTexture)
	 */
	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		mSurfaceTexture = surface;

		synchronized (mCallbacks) {
			// This is a linear search, but in practice we'll
			// have only a couple callbacks, so it doesn't matter.
			for (Callback callback : mCallbacks) {
				callback.surfaceDestroyed(this);
			}
		}

		if (this.mSurface != null) {
			this.mSurface.release();
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see SurfaceTextureListener#onSurfaceTextureUpdated(SurfaceTexture)
	 */
	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

}
