package com.myapp;

import android.app.Activity;

import java.util.Timer;
import java.util.TimerTask;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.android.PlanarYUVLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TestCamera extends Activity 
{
	/**
	 * SurfaceView是视图(View)的继承类，这个视图里内嵌了一个专门用于绘制的Surface。你可以控制这个Surface的格式和尺寸。
	 * Surfaceview控制这个Surface的绘制位置。 surface是纵深排序(Z-ordered)的，这表明它总在自己所在窗口的后面。
	 * surfaceview提供了一个可见区域，只有在这个可见区域内 的surface部分内容才可见，可见区域外的部分不可见。
	 * surface的排版显示受到视图层级关系的影响，它的兄弟视图结点会在顶端显示。这意味者 surface的内容会被它的兄弟视图遮挡，
	 * 这一特性可以用来放置遮盖物(overlays)(例如，文本和按钮等控件)。注意，如果surface上面 有透明控件，
	 * 那么它的每次变化都会引起框架重新计算它和顶层控件的透明效果，这会影响性能。 
	 */
	private SurfaceView sfvCamera;
	private SFHCamera sfhCamera;
	private ImageView imgView;
	private View centerView;
	private TextView txtScanResult;
	private Timer mTimer;
	private MyTimerTask mTimerTask;
	// 按照标准HVGA（HVGA：320*480/QVGA：240*320/WVGA：240*400）
	final static int width = 480;
	final static int height = 320;
	int dstLeft, dstTop, dstWidth, dstHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.setTitle("条码&二维码识别");
		imgView = (ImageView) findViewById(R.id.ImgView1);
		centerView = (View) findViewById(R.id.centerView);
		//surfaceview
		sfvCamera = (SurfaceView) findViewById(R.id.sfvCamera);
		//callback
		sfhCamera = new SFHCamera(sfvCamera.getHolder(), width, height,	previewCallback);
		txtScanResult = (TextView) findViewById(R.id.txtScanResult);
		Button back = (Button)findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i("退出啦", "back button");
				sfhCamera.surfaceDestroyed(null);
			}
		});
		// 初始化定时器
		mTimer = new Timer();
		mTimerTask = new MyTimerTask();
		mTimer.schedule(mTimerTask, 0, 80);
	}
	
	
	
	/**
	 * 每隔80ms调用一次MyTimerTask.run(),即自动对焦输出图片
	 * @author lxc
	 *
	 */
	class MyTimerTask extends TimerTask 
	{
		@Override
		public void run() 
		{
			if (dstLeft == 0) 
			{// 只赋值一次
				//activity.getWindowManager().getDefaultDisplay().getWidth();获取屏幕宽480
				dstLeft = centerView.getLeft() * width	/ getWindowManager().getDefaultDisplay().getWidth();
				Log.i("getWidth", getWindowManager().getDefaultDisplay().getWidth()+"");//480
				Log.i("getLeft", centerView.getLeft()+"");//15
				Log.i("dstLeft", dstLeft+"");//15
				//activity.getWindowManager().getDefaultDisplay().getHeight();获取屏幕高
				dstTop = centerView.getTop() * height	/ getWindowManager().getDefaultDisplay().getHeight();
				Log.i("getHeight", getWindowManager().getDefaultDisplay().getHeight()+"");//				
				Log.i("getTop", centerView.getTop()+"");//15
				Log.i("dstTop", dstTop+"");//15				
				
				dstWidth = (centerView.getRight() - centerView.getLeft())* width/ getWindowManager().getDefaultDisplay().getWidth();
				Log.i("getRight", centerView.getRight()+"");//15
				Log.i("dstWidth", dstWidth+"");//15						
				dstHeight = (centerView.getBottom() - centerView.getTop())* height/ getWindowManager().getDefaultDisplay().getHeight();
				Log.i("getBottom", centerView.getBottom()+"");//15
				Log.i("dstHeight", dstHeight+"");//15						
			}
			
			/**
			 * 最终将会调用到previewCallback方法
			 */
			sfhCamera.AutoFocusAndPreviewCallback();
		}
	}

	/**
	 * 匿名内部类
	 * 自动对焦后输出图片
	 */
	private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() 
	{
		@Override
		public void onPreviewFrame(byte[] data, Camera arg1) 
		{
			// 取得指定范围的帧的数据
			PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, dstLeft, dstTop, dstWidth, dstHeight,true);
			// 取得灰度图
			Bitmap mBitmap = source.renderCroppedGreyscaleBitmap();
			// 显示灰度图
			/**
			 * imageView的setImageResource和setImageBitmap这两种方法有什么区别，图片资源就在res下，为imageView赋值时用哪种方法?
			 * 第一种setImageResource 是从资源drawable中通过资源id找到文件转成可绘制对象drawable 然后绘制。这个方法会自动适配分辨率。
			 * 适用于不频繁设置图片图片资源不会太大的情况。 但是对于大图片时或者你需要不断的重复的设置图片 调用这个方法生成的drawable里一样会生成一个bitmap对象
			 *  因为bitmap是通过bitmapfactory生成的 有一部分要调用C库所以需要开辟一部分native本地内存空间以及一部分jvm的内存空间。
			 *  而native本地内存空间是C开辟的 jvm的gc垃圾回收器是回收不了这部分空间的，这个时候如果你频繁的调用setImageResource
			 *  且没有手动调recycle native的内存空间很难被释放掉。jvm的内存也不会及时得到回收这样就相当容易导致内存溢出。而setImageBitmap 
			 *  当你需要频繁设置大图片时 通过bitmapfactory生成bitmap然后设置 然后每次设置前将之前的bitmap手动掉recycle 置为可回收状态 这样很大程度能防止
			 *  内存泄露溢出所以看你的需求 你的图片是不频繁设置且不会太大就用第一种 如果需求不断的重复更新设置那最好用第二个并且记住手动及时回收后再设置 
			 *  如果有用到图片缓存的话则不要将大图片列入缓存中  图片的缓存模块最好只存储小且利用频繁的图片以节省内存和时间开销 大图则需做手动回收 
			 *  以保证低端点的机子不会oom
			 */
			imgView.setImageBitmap(mBitmap);
			//直接从图片中对二维码编码解码
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
			//解析多格式(二维)码
			MultiFormatReader reader = new MultiFormatReader();
			try 
			{
				Result result = reader.decode(bitmap);
				String strResult = "码类别:" + result.getBarcodeFormat().toString() 
						+ "结果：" + result.getText();
				txtScanResult.setText(strResult);
				sfhCamera.surfaceDestroyed(null);
				
			} 
			catch (Exception e) 
			{
				txtScanResult.setText("扫描中...");
			}
			
		}
	};

	
	protected void onDestroy()
	{
		sfhCamera.surfaceDestroyed(null);
		super.onDestroy();
	}
}
