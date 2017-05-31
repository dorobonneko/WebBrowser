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
import com.moe.utils.Theme;
import com.moe.fragment.preference.AdBkockFragment;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener
{
	private PreferenceFragment current,download,web,ad;

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
		current = this;
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
				if (download == null)download = new DownloadFragment();
				if (download.isAdded())
					getFragmentManager().beginTransaction().hide(this).show(download).commit();
				else
					getFragmentManager().beginTransaction().hide(this).add(R.id.main2, download).commit();
				current = download;
				break;
			case "setting_web":
				if (web == null)web = new WebFragment();
				if (web.isAdded())
					getFragmentManager().beginTransaction().hide(this).show(web).commit();
				else
					getFragmentManager().beginTransaction().hide(this).add(R.id.main2, web).commit();
				current = web;
				break;
			case "setting_ad":
				if (ad == null)ad = new AdBkockFragment();
				if (ad.isAdded())
					getFragmentManager().beginTransaction().hide(this).show(ad).commit();
				else
					getFragmentManager().beginTransaction().hide(this).add(R.id.main2, ad).commit();
				current = ad;
				break;
		}
		return false;
	}

	public boolean onBackPressed()
	{
		if (current != null)
		{
			if (current.onBackPressed())return true;
			else
			{
				getFragmentManager().beginTransaction().hide(current).show(this).commit();
				current = null;
				return true;
			}
		}
			return false;
	}
}
