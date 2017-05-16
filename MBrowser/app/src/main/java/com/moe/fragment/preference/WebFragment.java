package com.moe.fragment.preference;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import com.moe.preference.SeekBarPreference;
import android.preference.Preference;
import com.moe.widget.WebView;

public class WebFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener
{

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName("webview");
		addPreferencesFromResource(R.xml.web_setting);
		SeekBarPreference sb=(SeekBarPreference)findPreference("textSize");
		sb.setMax(150);
		sb.setDefaultValue(50);
		sb.add(50);
		findPreference("ua").setOnPreferenceChangeListener(this);
		if(getPreferenceManager().getSharedPreferences().getInt("ua",0)==3)
			findPreference("customUa").setEnabled(true);
			else
			findPreference("customUa").setEnabled(false);
	}

	@Override
	public boolean onPreferenceChange(Preference p1, Object p2)
	{
		switch(p1.getKey()){
			case "ua":
				switch((Integer)p2){
					case 0:
						android.webkit.WebView wv=new android.webkit.WebView(getActivity());
						getPreferenceManager().getSharedPreferences().edit().putString(WebView.Setting.USERAGENT,wv.getSettings().getUserAgentString()).commit();
						findPreference("customUa").setEnabled(false);
						wv.destroy();
						break;
					case 1:
						getPreferenceManager().getSharedPreferences().edit().putString(WebView.Setting.USERAGENT,getResources().getTextArray(R.array.uavalue)[1].toString()).commit();	
						findPreference("customUa").setEnabled(false);
						
						break;
					case 2:
						getPreferenceManager().getSharedPreferences().edit().putString(WebView.Setting.USERAGENT,getResources().getTextArray(R.array.uavalue)[2].toString()).commit();	
						findPreference("customUa").setEnabled(false);
						
						break;
					case 3:
						getPreferenceManager().getSharedPreferences().edit().putString(WebView.Setting.USERAGENT,getPreferenceManager().getSharedPreferences().getString("customUa","")).commit();
						findPreference("customUa").setEnabled(true);
						break;
				}
				break;
				case "customUa":
				getPreferenceManager().getSharedPreferences().edit().putString(WebView.Setting.USERAGENT,p2.toString()).commit();	
					break;
		}
		return true;
	}


	

	@Override
	public boolean onBackPressed()
	{
		// TODO: Implement this method
		return false;
	}
	
}
