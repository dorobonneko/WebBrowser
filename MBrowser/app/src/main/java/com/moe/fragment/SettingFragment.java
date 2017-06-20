package com.moe.fragment;
import android.app.Activity;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import android.preference.Preference;
import com.moe.fragment.preference.PreferenceFragment;
import com.moe.fragment.preference.DownloadFragment;
import com.moe.fragment.preference.WebFragment;
import android.view.View;
import android.content.res.TypedArray;
import android.app.FragmentTransaction;
import com.moe.fragment.preference.AdBkockFragment;
import android.content.Intent;
import java.io.File;
import com.moe.utils.DataUtils;
import com.moe.database.Download;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;
import java.io.FileNotFoundException;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener
{
	private PreferenceFragment current,download,web,ad;
	private int count=0;
	private boolean copying;
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		/**TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.colorPrimaryDark});
		 view.setBackgroundColor(ta.getColor(0,R.color.primary_dark));
		 ta.recycle();*/
		//Theme.registerBackground(view);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		home();
		getView().setId(90);
	}
	private void home()
	{

		addPreferencesFromResource(R.xml.setting);
		findPreference("setting_web").setOnPreferenceClickListener(this);
		findPreference("setting_ad").setOnPreferenceClickListener(this);
		findPreference("setting_download").setOnPreferenceClickListener(this);
		findPreference("setting_about").setOnPreferenceClickListener(this);

	}

	@Override
	public boolean onPreferenceClick(Preference p1)
	{
		switch (p1.getKey())
		{
			case "setting_download":
				if (download == null||download.isDetached())download = new DownloadFragment();
				if (download.isAdded())
					getFragmentManager().beginTransaction().hide(this).show(download).commit();
				else
					getFragmentManager().beginTransaction().hide(this).add(R.id.main2, download).commit();
				current = download;
				break;
			case "setting_web":
				if (web == null||web.isDetached())web = new WebFragment();
				if (web.isAdded())
					getFragmentManager().beginTransaction().hide(this).show(web).commit();
				else
					getFragmentManager().beginTransaction().hide(this).add(R.id.main2, web).commit();
				current = web;
				break;
			case "setting_ad":
				if (ad == null||ad.isDetached())ad = new AdBkockFragment();
				if (ad.isAdded())
					getFragmentManager().beginTransaction().hide(this).show(ad).commit();
				else
					getFragmentManager().beginTransaction().hide(this).add(R.id.main2, ad).commit();
				current = ad;
				break;
			case "setting_about":
				handler.removeMessages(0);
				handler.sendEmptyMessageDelayed(0,300);
				count++;
				break;
		}
		return false;
	}
	final Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg)
		{
			if(count==1){
				//单击
			}else{
				if(copying){
					Toast.makeText(getActivity(),"正在拷贝中",Toast.LENGTH_LONG).show();
				}else{
					Toast.makeText(getActivity(),"开始拷贝",Toast.LENGTH_LONG).show();
					new Thread(){
						public void run(){
							copying=true;
							try
							{
								DataUtils.copyFile(getActivity().getCacheDir().getParentFile(), new File(Download.Setting.DIR_DEFAULT, "data" + System.currentTimeMillis()));
							
							getActivity().runOnUiThread(new Runnable(){

									@Override
									public void run()
									{
										Toast.makeText(getActivity(),"拷贝完成",Toast.LENGTH_LONG).show();

									}
								});
							}
							catch (FileNotFoundException e)
							{getActivity().runOnUiThread(new Runnable(){

										@Override
										public void run()
										{
											Toast.makeText(getActivity(),"拷贝失败！",Toast.LENGTH_LONG).show();

										}
									});}
							copying=false;
							count=0;

						}
					}.start();
				}
			}
		}
	
};
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(current!=null)current.onActivityResult(requestCode,resultCode,data);
		}

	public boolean onBackPressed()
	{
		if (current != null)
		{
			if (current.onBackPressed())return true;
			else
			{
				getFragmentManager().beginTransaction().hide(current).detach(current).remove(current).show(this).commit();
				current = null;
				return true;
			}
		}
			return false;
	}
}
