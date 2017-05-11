package com.moe.application;
import com.moe.utils.CatchHandler;

public class Application extends android.app.Application
{

    @Override
    public void onCreate()
    {
        // TODO: Implement this method
        super.onCreate();
        CatchHandler.getInstance().init(getApplicationContext());
        //if(QbSdk.canLoadX5(this))
           // QbSdk.initX5Environment(this,null);
          //  else
           // QbSdk.forceSysWebView();
    }
    
}
