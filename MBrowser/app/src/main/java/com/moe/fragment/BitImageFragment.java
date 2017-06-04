package com.moe.fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.hardware.camera2.CameraManager;
import android.hardware.Camera;
import java.io.IOException;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.app.Activity;
import android.widget.Toast;
import android.graphics.PixelFormat;
import java.util.List;
import com.moe.widget.CameraBorder;
import com.moe.utils.Theme;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import android.os.Handler;
import android.os.Message;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Rect;
import com.moe.utils.BitImageParser;
import de.greenrobot.event.EventBus;
import com.moe.bean.WindowEvent;
import android.widget.ImageView;
import android.graphics.Canvas;
import android.database.Cursor;
import android.provider.MediaStore;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.File;;

public class BitImageFragment extends Fragment implements SurfaceHolder.Callback,Camera.AutoFocusCallback,Camera.PreviewCallback,BitImageParser.Callback,View.OnClickListener
{
	//相框尺寸
	private Camera camera;
	private CameraBorder cb;
	private SurfaceView sv;
	private SurfaceHolder sh;
	private boolean parsing;
	private ImageView flash;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.bitimage_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		sv = (SurfaceView)view.findViewById(R.id.bitimage_camera);
		cb = (CameraBorder)view.findViewById(R.id.bitimage_cameraBorder);
		Theme.registerForeGround(cb);
		flash = (ImageView)view.findViewById(R.id.bitimage_view_flash);
		view.findViewById(R.id.bitimage_view_back).setOnClickListener(this);
		view.findViewById(R.id.bitimage_view_pickerPicture).setOnClickListener(this);
		flash.setOnClickListener(this);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		cb.setSize(getActivity().getWindowManager().getDefaultDisplay().getWidth() / 2, getActivity().getWindowManager().getDefaultDisplay().getWidth() / 2);
		if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 22);
		}
		else
		{
			sh = sv.getHolder();
			sh.addCallback(this);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (requestCode == 22)
		{
			if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
			{
				sh = sv.getHolder();
				sh.addCallback(this);
			}
			else
			{
				Toast.makeText(getActivity(), "无权限", Toast.LENGTH_SHORT).show();
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	String url=null;
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode==0x36&&resultCode==Activity.RESULT_OK){
			
			if(data.getData().getScheme().equals("content")){
			Cursor c=getContext().getContentResolver().query(data.getData(),new String[]{MediaStore.Images.ImageColumns.DATA},null,null,null);
			if(c.moveToFirst()){
			url=c.getString(0);
			}
			c.close();
			}else{
				url=data.getDataString();
			}
			new Thread(){
				public void run(){
					parsing=true;
					BitImageParser.decodeImage(BitmapFactory.decodeFile(url), BitImageFragment.this);
				}
			}.start();
			
		}
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.bitimage_view_back:
				getActivity().onBackPressed();
				break;
			case R.id.bitimage_view_flash:
				Camera.Parameters param=camera.getParameters();
				if(param.getFlashMode().equals(param.FLASH_MODE_TORCH))
				{param.setFlashMode(param.FLASH_MODE_OFF);
				flash.setImageResource(R.drawable.ic_flash_off);
				}
					else{
					param.setFlashMode(param.FLASH_MODE_TORCH);
					flash.setImageResource(R.drawable.ic_flash);
					}
					camera.setParameters(param);
				break;
			case R.id.bitimage_view_pickerPicture:
				try{getActivity().startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),0x36);}
				catch(Exception e){}
				break;
		}
	}



	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if (hidden)
		{
			flash.setImageResource(R.drawable.ic_flash_off);
			getView().setKeepScreenOn(false);
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();}
		else
		{
			open();

		}
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onPause()
	{
		flash.setImageResource(R.drawable.ic_flash_off);
		super.onPause();
	}

	@Override
	public void surfaceCreated(SurfaceHolder p1)
	{
		open();
	}

	@Override
	public void surfaceChanged(SurfaceHolder p1, int p2, int p3, int p4)
	{

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder p1)
	{
		try
		{
			if (camera != null)
			{
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release();}
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onAutoFocus(boolean p1, final Camera p2)
	{


		p2.cancelAutoFocus();
		handler.sendEmptyMessageDelayed(0, 500);
		
	}

	@Override
	public void onPreviewFrame(final byte[] p1, Camera p2)
	{
		if (parsing)return;
		parsing = true;
		new Thread(){
			public void run()
			{
				Camera.Size size=camera.getParameters().getPreviewSize();
				BitImageParser.decodeImage(p1, BitImageFragment.this,size.width,size.height);				
			}
		}.start();
	}




	@Override
	public boolean onBackPressed()
	{

		return false;
	}

	private void open()
	{
		getView().setKeepScreenOn(true);
		camera = Camera.open();
		try
		{
			Camera.Parameters param=camera.getParameters();
			//List<Camera.Area> lca=new ArrayList<>();
			//lca.add(new Camera.Area(cb.getFocusArea(), 1));
			//param.setFocusAreas(lca);
			//List<Camera.Size> lcs=param.getSupportedPictureSizes();
			//Camera.Size size=lcs.get(0);
			//param.setPreviewSize(size.width, size.height);
			param.setRotation(90);
			param.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
			camera.setParameters(param);
			camera.setDisplayOrientation(90);
			camera.setPreviewDisplay(sh);
			camera.startPreview();
			camera.setPreviewCallback(this);
			camera.autoFocus(this);
		}
		catch (IOException e)
		{}
	}
	final Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				camera.autoFocus(BitImageFragment.this);
			}
			catch (Exception e)
			{}
		}

	};

	@Override
	public void onSuccess(String data)
	{
		parsing = false;
		getActivity().onBackPressed();
		EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_NEW_WINDOW, data));
	}

	@Override
	public void onFail(Exception e)
	{
		parsing = false;
		try
		{camera.cancelAutoFocus();
			handler.sendEmptyMessageDelayed(0, 500);
		}
		catch (Exception ee)
		{}
		if(url!=null){
			url=null;
			getView().post(new Runnable(){

					@Override
					public void run()
					{
						Toast.makeText(getActivity(),"识别失败",Toast.LENGTH_SHORT).show();
					}
				});
			}
	}



}
