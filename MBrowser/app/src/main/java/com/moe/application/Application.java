package com.moe.application;
import com.moe.utils.CatchHandler;

public class Application extends android.app.Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();
        CatchHandler.getInstance().init(getApplicationContext());
        
    }
    
}
