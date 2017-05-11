package com.moe.utils;
import android.content.Context;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import android.os.Looper;
import android.widget.Toast;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.ComponentName;


public class CatchHandler implements Thread.UncaughtExceptionHandler{

	private CatchHandler() {
	}

	public static CatchHandler getInstance() {

		return mCatchHandler;
	}

	private static CatchHandler mCatchHandler = new CatchHandler();

	private Context mContext;

	//private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
			handleException(thread,ex);
            
				android.os.Process.killProcess(android.os.Process.myPid());
	

	}

	public void init(Context context) {
		mContext = context;
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	private void ToastException(final Thread thread, final Throwable ex) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				handleException(thread,ex);
              Looper.loop();
			}
		}.start();
	}

	private void handleException(final Thread thread, final Throwable ex) {
		
		StringBuilder builder=new StringBuilder();
		StackTraceElement[] s= ex.getStackTrace();
        builder.append("请把以下信息发送给开发者！\n\n");
        builder.append(ex.getMessage()).append("\n");
		for(StackTraceElement ss:s){
			builder.append(ss.toString()).append("\n\n");
		}
        Intent intent=new Intent();
        intent.setComponent(new ComponentName("com.moe.Mbrowser","com.moe.Mbrowser.ExceptionActivity"));
        intent.putExtra(intent.EXTRA_TEXT,builder.toString());
        intent.setType("text/plain");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
