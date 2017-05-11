package com.moe.view;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import com.moe.Mbrowser.R;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.content.ClipboardManager;
import android.widget.Toast;
import de.greenrobot.event.EventBus;
import com.moe.bean.WindowEvent;
import android.view.MotionEvent;
public class PopupWindow implements View.OnClickListener
{
	private WebView.HitTestResult wh;
	private static PopupWindow pw;
	private android.widget.PopupWindow pop;
	private ClipboardManager cm;
	private Context context;
	private View url1,url2,url3,img_r,img_s;
	private int item_height=42;
	private PopupWindow(Context c){
		this.context=c;
		cm=(ClipboardManager)c.getSystemService(c.CLIPBOARD_SERVICE);
		pop = new android.widget.PopupWindow();
		View v=LayoutInflater.from(c).inflate(R.layout.popup_menu, null);
		pop.setContentView(v);
		pop.setWidth((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 62, c.getResources().getDisplayMetrics()));
		pop.setFocusable(true);
		pop.setTouchable(true);
		pop.setOutsideTouchable(true);
		pop.setBackgroundDrawable(new BitmapDrawable());
		url1=v.findViewById(R.id.popupmenu_copy_url);
		url1.setOnClickListener(this);
		url2=v.findViewById(R.id.popupmenu_open);
		url2.setOnClickListener(this);
		url3=v.findViewById(R.id.popupmenu_open_new);
		url3.setOnClickListener(this);
		//v.findViewById(R.id.popupmenu_property).setOnClickListener(this);
		img_r=v.findViewById(R.id.popupmenu_read_images);
		img_r.setOnClickListener(this);
		img_s=v.findViewById(R.id.popupmenu_save_image);
		img_s.setOnClickListener(this);
	
	}

	public void showAtLocation(View p0, int gravity, MotionEvent event)
	{
		switch (wh.getType())
		{
            case HitTestResult.ANCHOR_TYPE:
				//普通a标签
				break;
			case HitTestResult.SRC_ANCHOR_TYPE:
				//链接a标签
				url1.setVisibility(View.VISIBLE);
				url2.setVisibility(View.VISIBLE);
				url3.setVisibility(View.VISIBLE);
				img_r.setVisibility(View.GONE);
				img_s.setVisibility(View.GONE);
				pop.setHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, item_height*3, p0.getResources().getDisplayMetrics()));
				
				break;
            case HitTestResult.IMAGE_ANCHOR_TYPE:
			case HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
				//图片
				url1.setVisibility(View.VISIBLE);
				url2.setVisibility(View.VISIBLE);
				url3.setVisibility(View.VISIBLE);
				img_r.setVisibility(View.VISIBLE);
				img_s.setVisibility(View.VISIBLE);
				pop.setHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, item_height*5, p0.getResources().getDisplayMetrics()));
				
				break;
            case HitTestResult.EDIT_TEXT_TYPE:
				return;
			case HitTestResult.EMAIL_TYPE:
			case HitTestResult.GEO_TYPE:
			case HitTestResult.PHONE_TYPE:
				break;
            case HitTestResult.UNKNOWN_TYPE:
				break;
        }
		pop.showAtLocation(p0,gravity, (int)event.getX(), (int)event.getY() - pop.getHeight() / 2);
	}
	public void setHitTestResult(WebView.HitTestResult hitTestResult)
	{
		this.wh=hitTestResult;
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.popupmenu_copy_url:
				cm.setText(wh.getExtra());
				Toast.makeText(context,"已复制链接",Toast.LENGTH_SHORT).show();
				break;
			case R.id.popupmenu_open:
				EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_NEW_WINDOW_BACKGROUND,wh.getExtra()));
				break;
			case R.id.popupmenu_open_new:
			case R.id.popupmenu_read_images:
				EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_NEW_WINDOW,wh.getExtra()));
				break;
			case R.id.popupmenu_save_image:
				break;
		}
		pop.dismiss();
	}

	
	public static PopupWindow getInstance(Context c){
		if(pw==null)pw=new PopupWindow(c);
		return pw;
	}

	public android.widget.PopupWindow getPop()
	{
		return pop;
	}
}
