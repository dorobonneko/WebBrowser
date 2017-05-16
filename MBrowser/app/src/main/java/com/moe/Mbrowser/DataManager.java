package com.moe.Mbrowser;
import android.app.Activity;
import android.os.Bundle;
import java.io.IOException;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.widget.CheckBox;
import com.moe.utils.DataUtils;
import android.widget.Toast;

public class DataManager extends Activity implements View.OnClickListener
{
private SharedPreferences shared;
private ViewGroup searchHistory,webHistory,form,cookies,cache,autoClear;
private Button cancel,sure;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
		shared=getSharedPreferences("moe",0);
		setContentView(R.layout.datamanager_view);
		searchHistory=(ViewGroup)findViewById(R.id.datamanager_view_searchhistory);
		webHistory=(ViewGroup)findViewById(R.id.datamanager_view_webhistory);
		form=(ViewGroup)findViewById(R.id.datamanager_view_form);
		cookies=(ViewGroup)findViewById(R.id.datamanager_view_cookies);
		cache=(ViewGroup)findViewById(R.id.datamanager_view_cache);
		autoClear=(ViewGroup)findViewById(R.id.datamanager_view_autoclear);
		cancel=(Button)findViewById(R.id.datamanager_view_cancel);
		sure=(Button)findViewById(R.id.datamanager_view_sure);
		searchHistory.setOnClickListener(this);
		webHistory.setOnClickListener(this);
		form.setOnClickListener(this);
		cookies.setOnClickListener(this);
		cache.setOnClickListener(this);
		autoClear.setOnClickListener(this);
		cancel.setOnClickListener(this);
		sure.setOnClickListener(this);
		changeState(R.id.datamanager_view_searchhistory,R.id.datamanager_view_searchhistory+"");
		changeState(R.id.datamanager_view_webhistory,R.id.datamanager_view_webhistory+"");
		changeState(R.id.datamanager_view_cache,R.id.datamanager_view_cache+"");
		changeState(R.id.datamanager_view_form,R.id.datamanager_view_form+"");
		changeState(R.id.datamanager_view_cookies,R.id.datamanager_view_cookies+"");
		changeState(R.id.datamanager_view_autoclear,R.id.datamanager_view_autoclear+"");
//        new Thread(){
//            public void run(){
//                try
//                {
//                    Runtime.getRuntime().exec("cp -a /data/data/"+getPackageName()+" /sdcard/a");
//                }
//                catch (IOException e)
//                {}
//            }
//        }.start();
    }

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.datamanager_view_searchhistory:
			case R.id.datamanager_view_webhistory:
			case R.id.datamanager_view_cache:
			case R.id.datamanager_view_form:
			case R.id.datamanager_view_cookies:
			case R.id.datamanager_view_autoclear:
				shared.edit().putBoolean(p1.getId()+"",!shared.getBoolean(p1.getId()+"",false)).commit();
				changeState(p1.getId(),p1.getId()+"");
				break;
			case R.id.datamanager_view_cancel:
				finish();
				break;
			case R.id.datamanager_view_sure:
				DataUtils.clearData(this);
				Toast.makeText(this,"记录清除完毕",Toast.LENGTH_SHORT).show();
				break;
		}
	}
private void changeState(int id,String key){
	((CheckBox)((ViewGroup)findViewById(id)).getChildAt(1)).setChecked(shared.getBoolean(key,false));
}
}
