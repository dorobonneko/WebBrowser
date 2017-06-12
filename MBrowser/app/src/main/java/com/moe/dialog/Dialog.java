package com.moe.dialog;
import com.moe.Mbrowser.R;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.widget.TextView;
import android.widget.ImageView;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.app.Activity;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.util.TypedValue;

public class Dialog extends android.app.Dialog
{
    
    private TextView mTitle=null;
    private ImageView mIcon=null;
    private ViewGroup mView=null;
	private String title;
    public Dialog(Context context) {
        this(context,R.style.Dialog);
      
    }

    public Dialog(Context context, int themeResId) {
        super(context,themeResId);
       
       //setOwnerActivity((Activity)context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
       /** getWindow().setWindowAnimations(R.style.PopupWindowAnim);
        WindowManager.LayoutParams wl=getWindow().getAttributes();
        wl.gravity=Gravity.CENTER;
        wl.width=getWindow().getWindowManager().getDefaultDisplay().getWidth();
        getWindow().setAttributes(wl);
        getWindow().setBackgroundDrawableResource(R.color.accent);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                              WindowManager.LayoutParams.MATCH_PARENT);*/
        super.onCreate(savedInstanceState);
		getWindow().setDimAmount(0.3f);
        ViewGroup.LayoutParams vl=new ViewGroup.LayoutParams(getWindow().getWindowManager().getDefaultDisplay().getWidth(),ViewGroup.LayoutParams.WRAP_CONTENT);
       super.setContentView(LayoutInflater.from(getContext()).inflate(R.layout.dialog_view,null),vl);
        mTitle=(TextView)super.findViewById(R.id.dialogview_title);
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
        mIcon=(ImageView)super.findViewById(R.id.dialogview_icon);
       mView=(ViewGroup)super.findViewById(R.id.dialogview_content);
	   if(title!=null)mTitle.setText(title);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params)
    {
        mView.addView(view,params);
    }

    @Override
    public void setContentView(View view)
    {
        mView.addView(view);
    }

    @Override
    public void setContentView(int layoutResID)
    {
        mView.addView(LayoutInflater.from(getContext()).inflate(layoutResID,null));
    }

 
   

    
    public void setTitle(CharSequence title)
    {
		if(mTitle!=null)
        mTitle.setText(title);
		else this.title=title.toString();
    }
    public void setIcon(int resId){
        setIcon(getContext().getResources().getDrawable(resId));
    }
    public void setIcon(Drawable d){
        mIcon.setImageDrawable(d);
    }
}
