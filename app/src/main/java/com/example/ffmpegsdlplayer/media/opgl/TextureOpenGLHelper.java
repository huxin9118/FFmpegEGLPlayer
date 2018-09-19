/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ffmpegsdlplayer.media.opgl;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.example.ffmpegsdlplayer.media.tools.TextureRotationUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class TextureOpenGLHelper implements OpenGLHelper{

	public static final String VERTEX_SHADER =
			"attribute vec4 vPosition;" +
					"attribute vec2 inputTextureCoordinate;" +
					"varying vec2 textureCoordinate;" +
					"void main()" +
					"{"+
					"gl_Position = vPosition;"+
					"textureCoordinate = inputTextureCoordinate;" +
					"}";

	public static final String FRAGMENT_SHADER =
			"#extension GL_OES_EGL_image_external : require\n"+
					"precision mediump float;" +
					"varying vec2 textureCoordinate;\n" +
					"uniform samplerExternalOES s_texture;\n" +
					"void main() {" +
					"  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
					"}";

	public static final int ZOOM_INSIDE = 0;//适应屏幕
	public static final int ZOOM_ORIGINAL = 1;//原始
	public static final int ZOOM_STRETCH = 2;//拉伸
	public static final int ZOOM_CROP = 3;//裁剪
	public static final float CUBE[] = { -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, };
	// static final float CUBE[] = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, };

	private short drawOrder[] = { 0, 1, 2, 1, 2, 3 }; // order to draw vertices
	private static final int COORDS_PER_VERTEX = 2;
	private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

	public static int loadTexture() {
		int textures[] = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		checkGlError("glTexImage2D");

		//GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		return textures[0];
	}


	public static int loadShader(final String strSource, final int iType) {
		int[] compiled = new int[1];
		int iShader = GLES20.glCreateShader(iType);
		GLES20.glShaderSource(iShader, strSource);
		GLES20.glCompileShader(iShader);
		GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
			return 0;
		}
		return iShader;
	}

	public static int loadProgram(final String strVSource, final String strFSource) {
		int iVShader;
		int iFShader;
		int iProgId;

		iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER);
		if (iVShader == 0) {
			Log.d("Load Program", "Vertex Shader Failed");
			return 0;
		}
		iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
		if (iFShader == 0) {
			Log.d("Load Program", "Fragment Shader Failed");
			return 0;
		}

		iProgId = GLES20.glCreateProgram();

		GLES20.glAttachShader(iProgId, iVShader);
		GLES20.glAttachShader(iProgId, iFShader);

		GLES20.glLinkProgram(iProgId);
		int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES20.GL_TRUE) {
			Log.d("Load Program", "Linking Failed");
			GLES20.glDeleteProgram(iProgId);
			iProgId = 0;
		}
		GLES20.glDeleteShader(iVShader);
		GLES20.glDeleteShader(iFShader);
		return iProgId;
	}


	/**
	 * OpenGL iProgId
	 */
	private int mGLProgId;
	private int mTextureId;
	/**
	 * 是否初始化
	 */
	private boolean mIsInitialized;

//	private final FloatBuffer mGLCubeBuffer;
//	private final FloatBuffer mGLTextureBuffer;
	private final FloatBuffer vertexBuffer, textureVerticesBuffer;
	private final ShortBuffer drawListBuffer;

	private int mRotation;
	private boolean mFlipHorizontal;
	private boolean mFlipVertical;
	private float mOutputWidth;
	private float mOutputHeight;
	private float mImageWidth;
	private float mImageHeight;
	private int mZoom;

	private int mGLAttribAPosition;
	private int mGLAttribATextureCoord;

	/**
	 * 构造方法
	 */
	public TextureOpenGLHelper() {
		super();
//		mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
//		mGLCubeBuffer.put(CUBE).position(0);
//
//		mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
//				.order(ByteOrder.nativeOrder()).asFloatBuffer();

		ByteBuffer bb = ByteBuffer.allocateDirect(CUBE.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(CUBE);
		vertexBuffer.position(0);

		// initialize byte buffer for the draw list
		ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);

		float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHorizontal, mFlipVertical);
		ByteBuffer bb2 = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4);
		bb2.order(ByteOrder.nativeOrder());
		textureVerticesBuffer = bb2.asFloatBuffer();
		textureVerticesBuffer.put(textureCords);
		textureVerticesBuffer.position(0);

		setRotation(Surface.ROTATION_0, false, false);
	}

	public int getTextureId() {
		return mTextureId;
	}

	public void init(String strVSource, String strFSource) {
		mGLProgId = loadProgram(strVSource, strFSource);
		mTextureId = loadTexture();
		mGLAttribAPosition = GLES20.glGetAttribLocation(mGLProgId, "vPosition");
		mGLAttribATextureCoord = GLES20.glGetAttribLocation(mGLProgId, "inputTextureCoordinate");
		mIsInitialized = true;
	}


	public void onDrawFrame(Object data) {
		if (!mIsInitialized) {
			return;
		}

		SurfaceTexture surfaceTexture = (SurfaceTexture)data;
		surfaceTexture.updateTexImage();

		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		checkGlError("glClear");
		GLES20.glViewport(0, 0, (int) mOutputWidth, (int) mOutputHeight);
		checkGlError("glViewport");

		onDraw(vertexBuffer, textureVerticesBuffer);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
		checkGlError("glDrawElements");
		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mGLAttribAPosition);
		GLES20.glDisableVertexAttribArray(mGLAttribATextureCoord);
	}

	public void onDraw(final FloatBuffer vertexBuffer, final FloatBuffer textureVerticesBuffer) {
		GLES20.glUseProgram(mGLProgId);
		checkGlError("glUseProgram");

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);

		// Prepare the <insert shape here> coordinate data
		GLES20.glVertexAttribPointer(mGLAttribAPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
		GLES20.glEnableVertexAttribArray(mGLAttribAPosition);

		GLES20.glVertexAttribPointer(mGLAttribATextureCoord, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);
		GLES20.glEnableVertexAttribArray(mGLAttribATextureCoord);
	}

	private static void checkGlError(String tag) {
		int error = 0;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e("test", "After : " + tag + " Error : " + error);
		}
	}

	public void destory() {
		mIsInitialized = false;
		GLES20.glDeleteProgram(mGLProgId);
	}

	public void adjustImageScaling() {
		float outputWidth = mOutputWidth;
		float outputHeight = mOutputHeight;
		float imageWidth = mImageWidth;
		float imageHeight = mImageHeight;
		if (mRotation == Surface.ROTATION_270 || mRotation == Surface.ROTATION_90) {
			imageWidth = mImageHeight;
			imageHeight = mImageWidth;
		}

		float[] cube = CUBE;
		if(mZoom == ZOOM_INSIDE){
			float ratio1 = outputWidth / imageWidth;
			float ratio2 = outputHeight / imageHeight;
			float ratioMin = Math.min(ratio1, ratio2);
			int imageWidthNew = Math.round(imageWidth * ratioMin);
			int imageHeightNew = Math.round(imageHeight * ratioMin);
			float ratioWidth = imageWidthNew / outputWidth;
			float ratioHeight = imageHeightNew / outputHeight;
			cube = new float[]{CUBE[0] * ratioWidth, CUBE[1] * ratioHeight, CUBE[2] * ratioWidth, CUBE[3] * ratioHeight,
							CUBE[4] * ratioWidth, CUBE[5] * ratioHeight, CUBE[6] * ratioWidth, CUBE[7] * ratioHeight};
		}else if(mZoom == ZOOM_ORIGINAL){
			float ratioWidth = imageWidth / outputWidth;
			float ratioHeight = imageHeight / outputHeight;
			cube = new float[]{CUBE[0] * ratioWidth, CUBE[1] * ratioHeight, CUBE[2] * ratioWidth, CUBE[3] * ratioHeight,
					CUBE[4] * ratioWidth, CUBE[5] * ratioHeight, CUBE[6] * ratioWidth, CUBE[7] * ratioHeight};
		}else if(mZoom == ZOOM_STRETCH){
			cube = new float[]{CUBE[0], CUBE[1], CUBE[2], CUBE[3],
					CUBE[4], CUBE[5], CUBE[6], CUBE[7]};
		}else if(mZoom == ZOOM_CROP){
			float ratio1 = outputWidth / imageWidth;
			float ratio2 = outputHeight / imageHeight;
			float ratioMax = Math.max(ratio1, ratio2);
			int imageWidthNew = Math.round(imageWidth * ratioMax);
			int imageHeightNew = Math.round(imageHeight * ratioMax);

			float ratioWidth = imageWidthNew / outputWidth;
			float ratioHeight = imageHeightNew / outputHeight;
			cube = new float[]{CUBE[0] * ratioWidth, CUBE[1] * ratioHeight, CUBE[2] * ratioWidth, CUBE[3] * ratioHeight,
					CUBE[4] * ratioWidth, CUBE[5] * ratioHeight, CUBE[6] * ratioWidth, CUBE[7] * ratioHeight};
		}

		float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHorizontal, mFlipVertical);

		vertexBuffer.clear();
		vertexBuffer.put(cube).position(0);
		textureVerticesBuffer.clear();
		textureVerticesBuffer.put(textureCords).position(0);
	}

	public void setRotation(final int rotation, final boolean flipHorizontal, final boolean flipVertical) {
		mFlipHorizontal = flipHorizontal;
		mFlipVertical = flipVertical;
		boolean adjust = mRotation != rotation;

		mRotation = rotation;

		if (adjust) {
			adjustImageScaling();
		}
	}

	public void setZoom(final int zoom) {
		boolean adjust = mZoom != zoom;

		mZoom = zoom;

		if (adjust) {
			adjustImageScaling();
		}
	}

	public void onOutputSizeChanged(final int width, final int height) {
		boolean adjust = (mOutputWidth != width || mOutputHeight != height);

		mOutputWidth = width;
		mOutputHeight = height;

		if (adjust) {
			adjustImageScaling();
		}
	}

	public void onVideoSizeChanged(final int width, final int height) {
		boolean adjust = (mImageWidth != width || mImageHeight != height);

		mImageWidth = width;
		mImageHeight = height;

		if (adjust) {
			adjustImageScaling();
		}
	}
}
