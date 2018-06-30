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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.example.ffmpegsdlplayer.media.tools.TextureRotationUtil;

public class OpenGLHelper {
	/**
	 * NO_TEXTURE
	 */
	public static final int NO_TEXTURE = -1;
	/**
	 * NO_FILTER_VERTEX_SHADER
	 */
	public static final String NO_FILTER_VERTEX_SHADER = "attribute vec4 aPosition;\n"
			+ "attribute vec2 aTextureCoord;\n" + "varying vec2 vTextureCoord;\n" + "void main() {\n"
			+ "  gl_Position = aPosition;\n" + "  vTextureCoord = aTextureCoord;\n" + "}\n";
	 /**
	 * NO_FILTER_FRAGMENT_SHADER
	 */
	 public static final String NO_FILTER_FRAGMENT_SHADER = 
	"precision mediump float;\n" + "uniform sampler2D Ytex;\n"
	 + "uniform sampler2D Utex,Vtex;\n" + "varying vec2 vTextureCoord;\n" +
	 "void main(void) {\n"
	 + " float nx,ny,r,g,b,y,u,v;\n" + " mediump vec4 txl,ux,vx;\n" + "nx=vTextureCoord[0];\n"
	 + " ny=vTextureCoord[1];\n" + " y=texture2D(Ytex,vec2(nx,ny)).r;\n"
	 + " u=texture2D(Utex,vec2(nx,ny)).r;\n" + "v=texture2D(Vtex,vec2(nx,ny)).r;\n" +
	
	 // " y = v;\n"+
	 " y=1.1643*(y-0.0625);\n" + " u=u-0.5;\n" + " v=v-0.5;\n" +
	
	 " r=y+1.5958*v;\n" + " g=y-0.39173*u-0.81290*v;\n" + " b=y+2.017*u;\n"
	 + " gl_FragColor=vec4(r,g,b,1.0);\n" + "}\n";

//	/**
//	 * NO_FILTER_FRAGMENT_SHADER
//	 */
//	public static final String NO_FILTER_FRAGMENT_SHADER = "varying lowp vec2 vTextureCoord;\n"
//			+ "uniform sampler2D Ytex;\n" + "uniform sampler2D Utex;\n" + "uniform sampler2D Vtex;\n"
//			+ "void main(void)\n" + "{\n" + "mediump vec3 yuv;\n" + "lowp vec3 rgb;\n"
//			+ "yuv.x = texture2D(Ytex, vTextureCoord).r;\n" + "yuv.y = texture2D(Utex, vTextureCoord).r - 0.5;\n"
//			+ "yuv.z = texture2D(Vtex, vTextureCoord).r - 0.5;\n" + "rgb = mat3( 1, 1, 1,\n" + "0, -0.39465, 2.03211,\n"
//			+ "1.13983, -0.58060, 0) * yuv;\n" + "gl_FragColor = vec4(rgb, 1);\n" + "}\n";

//	 /**
//	 * NO_FILTER_FRAGMENT_SHADER
//	 */
//	 public static final String NO_FILTER_FRAGMENT_SHADER =
//	 "varying lowp vec2 vTextureCoord;\n" +
//	 "uniform sampler2D Ytex;\n" +
//	 "uniform sampler2D Utex;\n" +
//	 "uniform sampler2D Vtex;\n" +
//	 "void main(void)\n" +
//	 "{\n" +
//	 "float Y = (texture2D(Ytex, vTextureCoord).r - 16./255.)*1.164;\n" +
//	 "float U = texture2D(Utex, vTextureCoord).r - 128./255.;\n" +
//	 "float V = texture2D(Vtex, vTextureCoord).r - 128./255.;\n" +
//	 "float cr = clamp(Y + 1.596*U, 0. , 1.);\n" +
//	 "float cg = clamp(Y -0.813*U -0.392*V, 0. , 1.);\n" +
//	 "float cb = clamp(Y +2.017 *V, 0. , 1.);\n" +
//	 "vec4 ss= vec4(cb,cg,cr,1.);\n" +
//	 "gl_FragColor = ss;\n}";


	public static final int ZOOM_INSIDE = 0;//适应屏幕
	public static final int ZOOM_ORIGINAL = 1;//原始
	public static final int ZOOM_STRETCH = 2;//拉伸
	public static final int ZOOM_CROP = 3;//裁剪
	public static final float CUBE[] = { -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, };
	// static final float CUBE[] = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
	// 1.0f, };

	public byte[] buffer;

	public static int loadTexture(final Buffer data, final int width, final int height, final int usedTextId) {
		int textures[] = new int[1];
		if (usedTextId == NO_TEXTURE) {
			GLES20.glGenTextures(1, textures, 0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, width, height, 0, GLES20.GL_LUMINANCE,
					GLES20.GL_UNSIGNED_BYTE, data);
			checkGlError("glTexImage2D");
		} else {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTextId);
			GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width, height, GLES20.GL_LUMINANCE,
					GLES20.GL_UNSIGNED_BYTE, data);
			checkGlError("glTexSubImage2D");
			textures[0] = usedTextId;
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

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
		int[] link = new int[1];
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

		GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
		if (link[0] <= 0) {
			Log.d("Load Program", "Linking Failed");
			return 0;
		}
		GLES20.glDeleteShader(iVShader);
		GLES20.glDeleteShader(iFShader);
		return iProgId;
	}

	/**
	 * OpenGL User text Y id
	 */
	private int mUsedTextYId = OpenGLHelper.NO_TEXTURE;
	/**
	 * OpenGL User text U id
	 */
	private int mUsedTextUId = OpenGLHelper.NO_TEXTURE;
	/**
	 * OpenGL User text V id
	 */
	private int mUsedTextVId = OpenGLHelper.NO_TEXTURE;

	/**
	 * OpenGL iProgId
	 */
	private int mGLProgId;

	/**
	 * 是否初始化
	 */
	private boolean mIsInitialized;

	private final FloatBuffer mGLCubeBuffer;
	private final FloatBuffer mGLTextureBuffer;
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
	private int mGLUniformYtex;
	private int mGLUniformUtex;
	private int mGLUniformVtex;

	/**
	 * 构造方法
	 */
	public OpenGLHelper() {
		super();
		mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mGLCubeBuffer.put(CUBE).position(0);

		mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		setRotation(Surface.ROTATION_0, false, false);
	}

	/**
	 * <code>
	 * 	
	 * NO_FILTER_FRAGMENT_SHADER
	 *
	public static final String NO_FILTER_FRAGMENT_SHADER =   "precision mediump float;\n" +
			  "uniform sampler2D Ytex;\n" +
			  "uniform sampler2D Utex,Vtex;\n" +
			  "varying vec2 vTextureCoord;\n" +
			  "void main(void) {\n" +
			  "  float nx,ny,r,g,b,y,u,v;\n"  +
			  "  mediump vec4 txl,ux,vx;"  +
			  "  nx=vTextureCoord[0];\n"  +
			  "  ny=vTextureCoord[1];\n"  +
			  "  y=texture2D(Ytex,vec2(nx,ny)).r;\n"  +
			  "  u=texture2D(Utex,vec2(nx,ny)).r;\n"  +
			  "  v=texture2D(Vtex,vec2(nx,ny)).r;\n"  +
	
			  //"  y = v;\n"+
			  "  y=1.1643*(y-0.0625);\n"  +
			  "  u=u-0.5;\n"  +
			  "  v=v-0.5;\n"  +
	
			  "  r=y+1.5958*v;\n"  +
			  "  g=y-0.39173*u-0.81290*v;\n"  +
			  "  b=y+2.017*u;\n"  +
			  "  gl_FragColor=vec4(r,g,b,1.0);\n"  +
			  "}\n";
	 * </code> <code>
	 	"attribute vec4 aPosition;\n" +
			  "attribute vec2 aTextureCoord;\n" +
			  "varying vec2 vTextureCoord;\n" +
			  "void main() {\n" +
			  "  gl_Position = aPosition;\n" +
			  "  vTextureCoord = aTextureCoord;\n" +
			  "}\n"
	 * </code>
	 * 
	 * @param strVSource
	 * @param strFSource
	 */

	public void init(String strVSource, String strFSource) {
		GLES20.glClearColor(0, 0, 0, 1);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);

		mGLProgId = loadProgram(strVSource, strFSource);

		mGLAttribAPosition = GLES20.glGetAttribLocation(mGLProgId, "aPosition");
		mGLAttribATextureCoord = GLES20.glGetAttribLocation(mGLProgId, "aTextureCoord");
		mGLUniformYtex = GLES20.glGetUniformLocation(mGLProgId, "Ytex");
		mGLUniformUtex = GLES20.glGetUniformLocation(mGLProgId, "Utex");
		mGLUniformVtex = GLES20.glGetUniformLocation(mGLProgId, "Vtex");

		mIsInitialized = true;
	}


	public void onDrawFrame(byte[] buffer) {
		if (!mIsInitialized) {
			return;
		}
		this.buffer = buffer;
		int ySize = (int) (mImageWidth * mImageHeight);
		ByteBuffer yByteBuffer = ByteBuffer.allocate(ySize);
		ByteBuffer uByteBuffer = ByteBuffer.allocate(ySize / 4);
		ByteBuffer vByteBuffer = ByteBuffer.allocate(ySize / 4);

		yByteBuffer.put(buffer, 0, ySize);
		uByteBuffer.put(buffer, ySize, ySize / 4);
		vByteBuffer.put(buffer, ySize * 5 / 4, ySize / 4);

		yByteBuffer.position(0);
		uByteBuffer.position(0);
		vByteBuffer.position(0);

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		checkGlError("glClear");

		GLES20.glViewport(0, 0, (int) mOutputWidth, (int) mOutputHeight);
		checkGlError("glViewport");

		mUsedTextYId = OpenGLHelper.loadTexture(yByteBuffer, (int) mImageWidth, (int) mImageHeight, mUsedTextYId);

		checkGlError("onDrawFrame Y");

		mUsedTextUId = OpenGLHelper.loadTexture(uByteBuffer, ((int) mImageWidth) / 2, ((int) mImageHeight) / 2,
				mUsedTextUId);

		checkGlError("onDrawFrame U");

		mUsedTextVId = OpenGLHelper.loadTexture(vByteBuffer, ((int) mImageWidth) / 2, ((int) mImageHeight) / 2,
				mUsedTextVId);

		checkGlError("onDrawFrame V");

		onDraw(mGLCubeBuffer, mGLTextureBuffer);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		checkGlError("glDrawArrays");
		GLES20.glDisableVertexAttribArray(mGLAttribAPosition);
		GLES20.glDisableVertexAttribArray(mGLAttribATextureCoord);
	}

	public void onDraw(final FloatBuffer cubeBuffer, final FloatBuffer textureBuffer) {
		GLES20.glUseProgram(mGLProgId);
		checkGlError("glUseProgram");

		if (mUsedTextYId != NO_TEXTURE) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUsedTextYId);
			checkGlError("glBindTexture mUsedTextYId");
			GLES20.glUniform1i(mGLUniformYtex, 0);
			checkGlError("glUniform1i mUsedTextYId");
		}

		if (mUsedTextUId != NO_TEXTURE) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUsedTextUId);
			GLES20.glUniform1i(mGLUniformUtex, 1);
			checkGlError("glUniform1i mUsedTextUId");
		}

		if (mUsedTextVId != NO_TEXTURE) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
			checkGlError("glActiveTexture mUsedTextVId");

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mUsedTextVId);
			checkGlError("glBindTexture mUsedTextVId");

			GLES20.glUniform1i(mGLUniformVtex, 2);
			checkGlError("glUniform1i mUsedTextVId");
		}

		cubeBuffer.position(0);
		GLES20.glVertexAttribPointer(mGLAttribAPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
		GLES20.glEnableVertexAttribArray(mGLAttribAPosition);
		checkGlError("glVertexAttribPointer mGLAttribAPosition");

		textureBuffer.position(0);
		GLES20.glVertexAttribPointer(mGLAttribATextureCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
		checkGlError("glVertexAttribPointer mGLAttribATextureCoord");
		GLES20.glEnableVertexAttribArray(mGLAttribATextureCoord);
		checkGlError("glVertexAttribPointer mGLAttribATextureCoord");
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

		mGLCubeBuffer.clear();
		mGLCubeBuffer.put(cube).position(0);
		mGLTextureBuffer.clear();
		mGLTextureBuffer.put(textureCords).position(0);
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
