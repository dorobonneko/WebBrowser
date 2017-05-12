package com.moe.fragment;
import android.app.Activity;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import android.preference.Preference;
import com.moe.fragment.preference.PreferenceFragment;
import com.moe.fragment.preference.DownloadFragment;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener
{
	private PreferenceFragment current,download=new DownloadFragment();
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
			home();
			current=this;
			getView().setId(90);
		}
	private void home(){

		addPreferencesFromResource(R.xml.setting);
		findPreference("setting_web").setOnPreferenceClickListener(this);
		findPreference("setting_ad").setOnPreferenceClickListener(this);
		findPreference("setting_download").setOnPreferenceClickListener(this);
		findPreference("setting_about").setOnPreferenceClickListener(this);
		
	}
	
	@Override
	public boolean onPreferenceClick(Preference p1)
	{
		switch(p1.getKey()){
			case "setting_download":
				if(download.isAdded())
					getFragmentManager().beginTransaction().hide(this).show(download).commit();
					else
					getFragmentManager().beginTransaction().hide(this).add(R.id.main,download).commit();
				current=download;
				break;
		}
		return false;
	}

	public boolean onBackPressed(){
		if(current!=this){
			getFragmentManager().beginTransaction().hide(current).show(this).commit();
			current=this;
		return true;
		}
		return false;
	}
}
