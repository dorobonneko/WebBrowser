package com.moe.fragment.preference;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import com.moe.preference.SeekBarPreference;
import android.preference.Preference;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import com.moe.webkit.WebView;
import com.moe.webkit.WebSettings;

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
		findPreference("search").setOnPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference p1, Object p2)
	{
		switch(p1.getKey()){
			case "ua":
				switch((Integer)p2){
					case 0:
						android.webkit.WebView wv=new android.webkit.WebView(getActivity());
						getPreferenceManager().getSharedPreferences().edit().putString(WebSettings.Setting.USERAGENT,wv.getSettings().getUserAgentString()).commit();
						findPreference("customUa").setEnabled(false);
						wv.destroy();
						break;
					case 1:
						getPreferenceManager().getSharedPreferences().edit().putString(WebSettings.Setting.USERAGENT,getResources().getTextArray(R.array.uavalue)[1].toString()).commit();	
						findPreference("customUa").setEnabled(false);
						
						break;
					case 2:
						getPreferenceManager().getSharedPreferences().edit().putString(WebSettings.Setting.USERAGENT,getResources().getTextArray(R.array.uavalue)[2].toString()).commit();	
						findPreference("customUa").setEnabled(false);
						
						break;
					case 3:
						getPreferenceManager().getSharedPreferences().edit().putString(WebSettings.Setting.USERAGENT,getPreferenceManager().getSharedPreferences().getString("customUa","")).commit();
						findPreference("customUa").setEnabled(true);
						break;
				}
				break;
				case "customUa":
				getPreferenceManager().getSharedPreferences().edit().putString(WebSettings.Setting.USERAGENT,p2.toString()).commit();	
					break;
				case "search":
					switch((Integer)p2){
						case 3:
							TextInputLayout til=new TextInputLayout(getActivity());
							final EditText msg=new EditText(getActivity());
							til.addView(msg);
							til.setHint("关键词用$s代替");
							new AlertDialog.Builder(getActivity()).setTitle("自定义搜索引擎")
								.setNegativeButton("确定", new DialogInterface.OnClickListener(){

									@Override
									public void onClick(DialogInterface p1, int p2)
									{
										getPreferenceManager().getSharedPreferences().edit().putString("searchValue",msg.getText().toString()).commit();
									}
								})
							.setPositiveButton("取消",null)
							.setView(til)
							.show();
							break;
							default:
							getPreferenceManager().getSharedPreferences().edit().putString("searchValue",getResources().getTextArray(R.array.search_value)[(Integer)p2].toString()).commit();
							break;
					}
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
