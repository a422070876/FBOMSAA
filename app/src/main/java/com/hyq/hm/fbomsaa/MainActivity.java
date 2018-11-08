package com.hyq.hm.fbomsaa;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    private Handler multiHandler;
    private HandlerThread multiThread;
    private MultiEGLUtils multiEglUtils;
    private MultiRenderer multiRenderer;


    private Handler glHandler;
    private HandlerThread glThread;
    private EGLUtils eglUtils;
    private GLRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        multiThread = new HandlerThread("MultiThread");
        multiThread.start();
        multiHandler = new Handler(multiThread.getLooper());

        glThread = new HandlerThread("GLThread");
        glThread.start();
        glHandler = new Handler(glThread.getLooper());

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_jn);
        multiEglUtils = new MultiEGLUtils();
        multiRenderer = new MultiRenderer(bitmap);


        eglUtils = new EGLUtils();
        renderer = new GLRenderer(bitmap);

        SurfaceView multiSurfaceView = findViewById(R.id.multi_surface_view);
        multiSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(final SurfaceHolder holder, int format, final int width, final int height) {
                multiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        multiEglUtils.initEGL(holder.getSurface());
                        multiRenderer.onSurfaceChanged(width,height);

                        multiRenderer.onSurfaceCreated();

                        multiRenderer.onDrawFrame();
                        multiEglUtils.swap();
                    }
                });
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                multiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        multiRenderer.onSurfaceDestroyed();
                        multiEglUtils.release();
                    }
                });
            }
        });
        SurfaceView surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(final SurfaceHolder holder, int format, final int width, final int height) {
                glHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        eglUtils.initEGL(holder.getSurface());
                        renderer.onSurfaceChanged(width,height);
                        renderer.onSurfaceCreated();
                        renderer.onDrawFrame();
                        eglUtils.swap();
                    }
                });
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                glHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        renderer.onSurfaceDestroyed();
                        eglUtils.release();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        multiThread.quit();
        glThread.quit();
    }
}
