package com.moe.Mbrowser;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import com.moe.fragment.MainFragment;
import com.moe.fragment.Fragment;
import android.widget.Toast;
import android.view.Gravity;
import android.view.Menu;
import com.moe.bean.Message;
import de.greenrobot.event.Subscribe;
import com.moe.bean.MenuOptions;
import com.moe.fragment.BookmarksFragment;
import de.greenrobot.event.EventBus;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager;
import android.view.Window;
import android.content.SharedPreferences;
import android.content.Intent;
import com.moe.bean.DownloadItem;
import com.moe.dialog.DownloadDialog;
import com.moe.dialog.AlertDialog;

public class HomeActivity extends FragmentActivity
{
private SharedPreferences shared;
private Fragment current,main=new MainFragment(),bookmark=new BookmarksFragment();
private DownloadDialog dd;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        long i=System.currentTimeMillis();
		shared=getSharedPreferences("moe",0);
        setContentView(R.layout.main);
        getSupportFragmentManager().beginTransaction().add(R.id.main,main).commit();
		current=main;
		dd=new DownloadDialog(this);
		if(shared.getBoolean("full",false))
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
			((MainFragment)main).setPadding(true);
		}
		startService(new Intent(this,ResourceService.class));
        Toast.makeText(this,"启动耗时："+(System.currentTimeMillis()-i),300).show();
		EventBus.getDefault().register(this);
    }

	@Subscribe
	public void onDownload(DownloadItem di){
		dd.show(di);
	}
	@Subscribe
	public void onEvent(MenuOptions mo){
		switch(mo){
			case BOOKMARKS:
				if(bookmark.isAdded())
					getSupportFragmentManager().beginTransaction().hide(current).show(bookmark).commit();
					else
					getSupportFragmentManager().beginTransaction().hide(current).add(R.id.main,bookmark).commit();
					current=bookmark;
				break;
			case DOWNLOAD:
				startService(new Intent(this,DownloadService.class));
				break;
			case HOME:
				if(current!=main){
					getSupportFragmentManager().beginTransaction().hide(current).show(main).commit();
					current=main;
				}
				break;
			case FULLSCREEN:
				if(!shared.getBoolean("full",false))
				{
					getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
					shared.edit().putBoolean("full",true).commit();
					((MainFragment)main).setPadding(true);
				}else{
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				shared.edit().putBoolean("full",false).commit();
					((MainFragment)main).setPadding(false);
				}
				
				break;
			case EXIT:
				super.onBackPressed();
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode==RESULT_OK){
			switch(requestCode){
				case 233:
					dd.setPath(data.getStringExtra("dir"));
					break;
			}
		}
	}

	
	@Override
	protected void onDestroy()
	{
		
		EventBus.getDefault().unregister(this);
		stopService(new Intent(this,ResourceService.class));
		super.onDestroy();
	}

    @Override
    public void onBackPressed()
    {
		if(!current.onBackPressed()){
		if(current!=main){getSupportFragmentManager().beginTransaction().hide(current).show(main).commit();current=main;}
		else if(!main.onBackPressed())
        super.onBackPressed();
		}
    }
        
}
