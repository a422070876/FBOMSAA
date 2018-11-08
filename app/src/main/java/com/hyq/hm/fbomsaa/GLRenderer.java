package com.hyq.hm.fbomsaa;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by 海米 on 2018/10/26.
 */

public class GLRenderer {
    private int programId;
    private int aPositionHandle;
    private int uTextureSamplerHandle;
    private int aTextureCoordHandle;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureVertexBuffer;
    private int textureWidth = 100;
    private int textureHeight = 100;
    
    private int[] textures = new int[1];

    private Bitmap bitmap;

    public GLRenderer(Bitmap bitmap){
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


        GLES30.glGenTextures(1,textures,0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,textures[0]);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D,0,GLES30.GL_RGBA,bitmap,0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0);
    }

    public void onSurfaceDestroyed(){
        GLES30.glDeleteProgram(programId);
        GLES30.glDeleteTextures(1,textures,0);
    }
    private Rect rect = new Rect();
    public void onSurfaceChanged(int screenWidth, int screenHeight) {
        int left,top,viewWidth,viewHeight;
        float sh = screenWidth*1.0f/screenHeight;
        float vh = textureWidth*1.0f/textureHeight;
        if(sh < vh){
            left = 0;
            viewWidth = screenWidth;
            viewHeight = (int)(textureHeight*1.0f/textureWidth*viewWidth);
            top = (screenHeight - viewHeight)/2;
        }else{
            top = 0;
            viewHeight = screenHeight;
            viewWidth = (int)(textureWidth*1.0f/textureHeight*viewHeight);
            left = (screenWidth - viewWidth)/2;
        }
        rect.set(left,top,viewWidth,viewHeight);
    }

    public void onDrawFrame(){
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
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0);
    }
}
