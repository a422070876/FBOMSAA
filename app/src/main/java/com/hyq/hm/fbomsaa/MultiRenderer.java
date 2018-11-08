package com.hyq.hm.fbomsaa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by 海米 on 2018/10/26.
 */

public class MultiRenderer {
    private int programId;
    private int aPositionHandle;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;


    private int[] textures;

    private int[] frameBuffers;

    private int[] frameColors;


    private FloatBuffer vertexBuffer;
    private FloatBuffer textureVertexBuffer;

    
    private int textureWidth = 100;
    private int textureHeight = 100;

    private int frameWidth,frameHeight,screenWidth,screenHeight;

    private Bitmap bitmap;

    public MultiRenderer(Bitmap bitmap){
        float[] vertexData = {
                0.5f, -0.3f,0,
                -0.6f, -0.4f,0,
                0.2f, 0.3f,0,
                -0.4f, 0.7f,0
        };
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);
        float[] textureVertexData = {
                1f, 0f,//右下
                0f, 0f,//左下
                1f, 1f,//右上
                0f, 1f//左上
        };
        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        this.bitmap = bitmap;
        textureWidth = bitmap.getWidth();
        textureHeight = bitmap.getHeight();
    }


    public void onSurfaceCreated(){
        String fragmentShader = "varying highp vec2 vTexCoord;\n" +
                "uniform sampler2D sTexture;\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D(sTexture,vTexCoord);\n" +
                "}";
        String vertexShader = "attribute vec4 aPosition;\n" +
                "attribute vec2 aTexCoord;\n" +
                "varying vec2 vTexCoord;\n" +
                "void main() {\n" +
                "    vTexCoord=vec2(aTexCoord.x,1.0 - aTexCoord.y);\n" +
                "    gl_Position = aPosition;\n" +
                "}";
        programId= ShaderUtils.createProgram(vertexShader, fragmentShader);
        aPositionHandle = GLES30.glGetAttribLocation(programId,"aPosition");
        aPositionHandle = GLES30.glGetAttribLocation(programId, "aPosition");
        uTextureSamplerHandle=GLES30.glGetUniformLocation(programId,"sTexture");
        aTextureCoordHandle=GLES30.glGetAttribLocation(programId,"aTexCoord");


        textures = new int[2];
        GLES30.glGenTextures(2,textures,0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textures[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D,0,GLES30.GL_RGBA,bitmap,0);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[1]);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,frameWidth,frameHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

        //初始化colorBuffer
        frameColors = new int[1];
        GLES30.glGenRenderbuffers(1, frameColors, 0);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, frameColors[0]);
        //可以和glRenderbufferStorage对比一下试试
        //GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER,GLES30.GL_RGBA8,frameWidth, frameHeight);
        GLES30.glRenderbufferStorageMultisample(GLES30.GL_RENDERBUFFER,4,GLES30.GL_RGBA8,frameWidth, frameHeight);
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, 0);
        frameBuffers = new int[2];
        GLES30.glGenFramebuffers(2, frameBuffers,0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffers[0]);
        //绑定colorBuffer
        GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,GLES30.GL_RENDERBUFFER, frameColors[0]);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffers[1]);
        //绑定纹理
        GLES30.glFramebufferTexture2D(GLES30.GL_DRAW_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, textures[1], 0);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
    }

    public void onSurfaceDestroyed(){
        GLES30.glDeleteProgram(programId);
        GLES30.glDeleteTextures(2,textures,0);
        GLES30.glDeleteRenderbuffers(1, frameColors, 0);
        GLES30.glDeleteFramebuffers(2, frameBuffers,0);
    }
    private Rect rect = new Rect();
    public void onSurfaceChanged(int screenWidth, int screenHeight) {
        //设置frameBuffer的宽高,必须和SurfaceView宽高等比
        frameWidth = screenWidth;
        frameHeight = screenHeight;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        int left,top,viewWidth,viewHeight;
        float sh = frameWidth*1.0f/frameHeight;
        float vh = textureWidth*1.0f/textureHeight;
        if(sh < vh){
            left = 0;
            viewWidth = frameWidth;
            viewHeight = (int)(textureHeight*1.0f/textureWidth*viewWidth);
            top = (frameHeight - viewHeight)/2;
        }else{
            top = 0;
            viewHeight = frameHeight;
            viewWidth = (int)(textureWidth*1.0f/textureHeight*viewHeight);
            left = (frameWidth - viewWidth)/2;
        }
        rect.set(left,top,viewWidth,viewHeight);
    }

    public void onDrawFrame(){
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffers[0]);
        //渲染到frameBuffer
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT | GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glViewport(rect.left,rect.top,rect.right,rect.bottom);
        GLES30.glUseProgram(programId);
        GLES30.glEnableVertexAttribArray(aPositionHandle);
        GLES30.glVertexAttribPointer(aPositionHandle, 3, GLES30.GL_FLOAT, false,
                0, vertexBuffer);
        GLES30.glEnableVertexAttribArray(aTextureCoordHandle);
        GLES30.glVertexAttribPointer(aTextureCoordHandle,2,GLES30.GL_FLOAT,false,0,textureVertexBuffer);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textures[0]);
        GLES30.glUniform1i(uTextureSamplerHandle,0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
        //将frameBuffer的图像渲染到纹理里
        GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, frameBuffers[1]);
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, frameBuffers[0]);
        GLES30.glBlitFramebuffer(0, 0, frameWidth, frameHeight,
                0, 0,frameWidth, frameHeight,
                GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_LINEAR);
        //将纹理的图像渲染surfaceView上
        GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, frameBuffers[1]);
        GLES30.glBlitFramebuffer(0, 0, frameWidth, frameHeight,
                0, 0,screenWidth, screenHeight,
                GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_LINEAR);
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0);
    }
}
