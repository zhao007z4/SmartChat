package com.zyx.smarttouch.scan;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.zxing.camera.CameraManager;
import com.zxing.decoding.InactivityTimer;
import com.zxing.view.ViewfinderView;
import com.zyx.smarttouch.R;

import java.io.IOException;
import java.util.Vector;

public class ImageScanActivity extends AppCompatActivity implements Callback
{
	private ScanActivityHandler handler;
	private ViewfinderView viewfinderView;
	private SurfaceView preview_view;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_scan);

		FrameLayout flScan = (FrameLayout) findViewById(R.id.flScan);
		preview_view = (SurfaceView) findViewById(R.id.svScan);

		AttributeSet attrs = null;
		viewfinderView = new ViewfinderView(this, attrs);
		LayoutParams lps = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		flScan.addView(viewfinderView, lps);

		CameraManager.init(getApplication());
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		SurfaceView surfaceView = preview_view;
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface)
		{
			initCamera(surfaceHolder);
		}
		else
		{
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (handler != null)
		{
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy()
	{
		if (inactivityTimer != null)
		{
			inactivityTimer.shutdown();
		}
		inactivityTimer = null;
		super.onDestroy();
	}

	private void initCamera(final SurfaceHolder surfaceHolder)
	{
		try {
			CameraManager.get().openDriver(surfaceHolder, preview_view.getWidth(), preview_view.getHeight());
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new ScanActivityHandler(ImageScanActivity.this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		if (!hasSurface)
		{
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView()
	{
		return viewfinderView;
	}

	public Handler getHandler()
	{
		return handler;
	}

	public void drawViewfinder()
	{
		viewfinderView.drawViewfinder();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void handleDecode(Result obj, Bitmap barcode)
	{
		handler.removeMessages(ScanActivityHandler.HANLDE_UPLOAD);
		Bundle bundle = new Bundle();
		bundle.putString("result", obj.getText());
		Message msg = new Message();
		msg.what = ScanActivityHandler.HANLDE_UPLOAD;
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	public void UploadResult(Message msMessage)
	{
        Bundle bundle = msMessage.getData();
		if (bundle == null || !bundle.containsKey("result")) {
			return;
		}
		String sValue = bundle.getString("result");
        Intent intent = new Intent();
        intent.putExtra("value",sValue);
        setResult(Activity.RESULT_OK,intent);

        this.finish();
	}
}
