package com.moe.Mbrowser;
import android.app.Activity;
import android.os.Bundle;
import java.io.IOException;

public class DataManager extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        new Thread(){
            public void run(){
                try
                {
                    Runtime.getRuntime().exec("cp -a /data/data/"+getPackageName()+" /sdcard/a");
                }
                catch (IOException e)
                {}
            }
        }.start();
    }
    
}
