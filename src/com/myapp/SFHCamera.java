package com.myapp;

import java.io.IOException;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * surface是用来绘图的，比如照相录像界面，而surfaceView是用来显示surface所绘制的图像，可以通过SurfaceHolder来访问这个
 * surface，而SurfaceView.getHolder()方法就是用来返回SurfaceHolder对象以便访问surface,而holder.
 * addCallback(this)是因为
 * 当前实现了接口SurfaceHolder.Callback，所以this也是Callback的对象，在surface的各生命周期(create
 * change destroy) 中会调用重写的三方法
 * 正因调用了holder.addCallback(this)，就将当前重写的三个方法与surface关联起来了
 */
public class SFHCamera implements SurfaceHolder.Callback {
	private SurfaceHolder holder = null;
	private Camera mCamera;
	private int width, height;
	private Camera.PreviewCallback previewCallback;

	/**
	 * SurfaceHolder：
	 * 可以通过SurfaceHolder接口访问这个Surface.用getHolder()方法可以得到这个接口。 surfaceview变得可见时
	 * ，surface被创建；surfaceview隐藏前，
	 * surface被销毁。这样能节省资源。如果你要查看 surface被创建和销毁的时机，可以重载surfaceCreated
	 * (SurfaceHolder)和 surfaceDestroyed(SurfaceHolder) SurfaceHolder.Callback：
	 * 用户可以实现此接口接收surface变化的消息
	 * 。当用在一个SurfaceView 中时，它只在SurfaceHolder.Callback.surfaceCreated()
	 * 和SurfaceHolder
	 * .Callback.surfaceDestroyed()之间有效。设置Callback的方法是SurfaceHolder.addCallback
	 */
	public SFHCamera(SurfaceHolder holder, int w, int h,
			Camera.PreviewCallback previewCallback) {
		/**
		 * 这样就可以通过callback（）对SurfaceView进行修改。 this指代Callback的对象
		 */
		this.holder = holder;
		// 为SurfaceHolder添加一个SurfaceHolder.Callback回调接口
		this.holder.addCallback(this);
		/**
		 * 设置Surface的类型，接收如下的参数： SURFACE_TYPE_NORMAL：用RAM缓存原生数据的普通Surface
		 * SURFACE_TYPE_HARDWARE：适用于DMA(Direct memory access )引擎和硬件加速的Surface
		 * SURFACE_TYPE_GPU：适用于GPU加速的Surface
		 * SURFACE_TYPE_PUSH_BUFFERS：表明该Surface不包含原生数据
		 * ，Surface用到的数据由其他对象提供，在Camera图像预览中就使用该类型的Surface，
		 * 有Camera负责提供给预览Surface数据
		 * ，这样图像预览会比较流畅。如果设置这种类型则就不能调用lockCanvas来获取Canvas对象了。
		 * 需要注意的是，在高版本的Android SDK中，setType这个方法已经被depreciated了。
		 * 2、3、4中的同步锁机制的目的，就是为了在绘制的过程中，Surface中的数据不会被改变。
		 */
		this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		width = w;
		height = h;
		this.previewCallback = previewCallback;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(width, height);// 设置尺寸
		parameters.setPictureFormat(PixelFormat.JPEG);
		mCamera.setParameters(parameters);
		mCamera.startPreview();// 开始预览
		Log.e("Camera", "surfaceChanged");
	}

	/**
	 * 当surface对象创建后，该方法就会被立即调用。
	 */
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		mCamera = Camera.open();// 启动服务
		try {
			mCamera.setPreviewDisplay(holder);// 设置预览
			Log.e("Camera", "surfaceCreated");
		} catch (IOException e) {
			mCamera.release();// 释放
			mCamera = null;
		}
	}

	/**
	 * 当surface对象在将要销毁前，该方法会被立即调用。
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();// 停止预览
		mCamera = null;
		Log.e("Camera", "surfaceDestroyed");
	}

	/**
	 * 自动对焦并回调Camera.PreviewCallback
	 */
	public void AutoFocusAndPreviewCallback() {
		if (mCamera != null)
			mCamera.autoFocus(mAutoFocusCallBack);
	}

	/**
	 * 自动对焦
	 */
	private Camera.AutoFocusCallback mAutoFocusCallBack = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) { // 对焦成功，回调Camera.PreviewCallback
				mCamera.setOneShotPreviewCallback(previewCallback);
			}
		}
	};
}
