package com.moe.Mbrowser;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;
import android.widget.ScrollView;

public class ExceptionActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        ScrollView sv=new ScrollView(this);
        TextView tv=new TextView(this);
        sv.addView(tv);
        setContentView(sv);
        sv.setFitsSystemWindows(true);
        tv.setText(getIntent().getStringExtra(Intent.EXTRA_TEXT));
    }

    @Override
    public void onBackPressed()
    {
        // TODO: Implement this method
        super.onBackPressed();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    
}
