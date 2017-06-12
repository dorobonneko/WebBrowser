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
import android.graphics.Color;

public class DataManager extends Activity implements View.OnClickListener
{
private SharedPreferences shared;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
		shared=getSharedPreferences("moe",0);
		setContentView(R.layout.datamanager_view);
		int color=Color.parseColor(getResources().getTextArray(R.array.skin_color)[shared.getInt("color",0)].toString());
		int red=Color.red(color);
		int green=Color.green(color);
		int blue=Color.blue(color);
		findViewById(R.id.datamanager).setBackgroundColor(Color.rgb(red-30<0?0:red-30,green-30<0?0:green-30,blue-30<0?0:blue-30));
		findViewById(R.id.datamanager_view_searchhistory).setOnClickListener(this);
		findViewById(R.id.datamanager_view_webhistory).setOnClickListener(this);;
		findViewById(R.id.datamanager_view_form).setOnClickListener(this);
		findViewById(R.id.datamanager_view_cookies).setOnClickListener(this);
		findViewById(R.id.datamanager_view_cache).setOnClickListener(this);
		findViewById(R.id.datamanager_view_autoclear).setOnClickListener(this);
		findViewById(R.id.datamanager_view_cancel).setOnClickListener(this);
		findViewById(R.id.datamanager_view_sure).setOnClickListener(this);
		findViewById(R.id.datamanager_view_outprograme).setOnClickListener(this);
		changeState(R.id.datamanager_view_searchhistory,R.id.datamanager_view_searchhistory+"");
		changeState(R.id.datamanager_view_webhistory,R.id.datamanager_view_webhistory+"");
		changeState(R.id.datamanager_view_cache,R.id.datamanager_view_cache+"");
		changeState(R.id.datamanager_view_form,R.id.datamanager_view_form+"");
		changeState(R.id.datamanager_view_cookies,R.id.datamanager_view_cookies+"");
		changeState(R.id.datamanager_view_autoclear,R.id.datamanager_view_autoclear+"");
		changeState(R.id.datamanager_view_outprograme,R.id.datamanager_view_outprograme+"");
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
			case R.id.datamanager_view_cancel:
				finish();
				break;
			case R.id.datamanager_view_sure:
				DataUtils.clearData(this);
				Toast.makeText(this,"记录清除完毕",Toast.LENGTH_SHORT).show();
				break;
			default:
				shared.edit().putBoolean(p1.getId()+"",!shared.getBoolean(p1.getId()+"",false)).commit();
				changeState(p1.getId(),p1.getId()+"");
			break;
		}
	}
private void changeState(int id,String key){
	((CheckBox)((ViewGroup)findViewById(id)).getChildAt(1)).setChecked(shared.getBoolean(key,false));
}
}
