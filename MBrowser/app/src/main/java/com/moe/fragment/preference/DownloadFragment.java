package com.moe.fragment.preference;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import com.moe.database.Download;
import android.preference.SwitchPreference;
import android.preference.Preference;
import com.moe.preference.SeekBarPreference;
import android.content.Intent;
import com.moe.Mbrowser.FileExplorer;

public class DownloadFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener,Preference.OnPreferenceClickListener
{

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		// TODO: Implement this method
		super.onActivityCreated(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("download");
		addPreferencesFromResource(R.xml.download_setting);
		findPreference(Download.Setting.MULTITHREAD).setOnPreferenceChangeListener(this);
		SeekBarPreference sbp=(SeekBarPreference)findPreference(Download.Setting.THREADSIZE);
		sbp.setEnabled(getPreferenceManager().getSharedPreferences().getBoolean(Download.Setting.MULTITHREAD,Download.Setting.MULTITHREAD_DEFAULT));
		sbp.setMax(Runtime.getRuntime().availableProcessors()*2-1);
		sbp.setDefaultValue(Runtime.getRuntime().availableProcessors()-1);
		findPreference(Download.Setting.DIR).setOnPreferenceClickListener(this);
		findPreference(Download.Setting.DIR).setSummary(getPreferenceManager().getSharedPreferences().getString(Download.Setting.DIR,Download.Setting.DIR_DEFAULT));
	}

	@Override
	public boolean onPreferenceChange(Preference p1, Object p2)
	{
		switch(p1.getKey()){
			case Download.Setting.MULTITHREAD:
				findPreference(Download.Setting.THREADSIZE).setEnabled(p2);
				break;
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference p1)
	{
		switch(p1.getKey()){
			case Download.Setting.DIR:
				startActivity(new Intent(getActivity(),FileExplorer.class).putExtra("path",p1.getSummary().toString()));
				break;
		}
		return false;
	}


	
	@Override
	public boolean onBackPressed()
	{
		// TODO: Implement this method
		return false;
	}

	
}
