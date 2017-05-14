package com.moe.preference;
import android.preference.Preference;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AbsoluteLayout;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.AbsListView;
import android.util.TypedValue;
import android.content.res.TypedArray;
import com.moe.utils.Theme;

public class SeekBarPreference extends Preference implements SeekBar.OnSeekBarChangeListener
{
	private TextView start,current,end,title;
	private SeekBar sb;
	private int index=-1;
	private final static String xmlns="http://schemas.android.com/apk/res/android";
	private String mTitle;
	private int add=1;
	private boolean enable=true;
	private int max;
	public SeekBarPreference(Context context){
		super(context);
	}
	public SeekBarPreference(Context context,AttributeSet attrs){
		super(context,attrs);
		mTitle=attrs.getAttributeValue(xmlns,"title");
	}
	public SeekBarPreference(Context context,AttributeSet attrs,int style){
		super(context,attrs,style);
	}
	public SeekBarPreference(Context context,AttributeSet attrs,int style,int res){
		super(context,attrs,style,res);
	}

	@Override
	protected View onCreateView(ViewGroup parent)
	{
		LinearLayout content=new LinearLayout(getContext());
		content.setOrientation(content.VERTICAL);
		RelativeLayout ll=new RelativeLayout(getContext());
		//ll.setBackgroundColor(0xff0ff0ff);
		start=new TextView(getContext());
		current=new TextView(getContext());
		end=new TextView(getContext());
		title=new TextView(getContext());
		RelativeLayout.LayoutParams rl=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.ALIGN_PARENT_LEFT|RelativeLayout.CENTER_VERTICAL);
		ll.addView(start,rl);
		rl=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.CENTER_IN_PARENT);
		ll.addView(current,rl);
		rl=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rl.addRule(RelativeLayout.CENTER_VERTICAL);
		ll.addView(end,rl);
		rl=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		rl.addRule(RelativeLayout.ALIGN_PARENT_TOP|RelativeLayout.CENTER_HORIZONTAL);
		ll.addView(title,rl);
		sb=new SeekBar(getContext());
		content.addView(ll,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT,1f));
		content.addView(sb);
		//View v=super.onCreateView(parent);
		//content.setPadding(0,6,0,6);
		content.setPaddingRelative((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,getContext().getResources().getDisplayMetrics()),0,(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,getContext().getResources().getDisplayMetrics()),0);
		//content.setLayoutParams());
		TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.R.attr.listPreferredItemHeight});
		content.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,(int)ta.getDimension(0,0)));
		ta.recycle();
		//content.measure(v.getMeasuredWidth(),v.getMeasuredHeight());
		return content;
	}

	@Override
	protected void onBindView(View view)
	{
		sb.setOnSeekBarChangeListener(this);
		sb.setProgress(index);
		if(mTitle!=null)title.setText(mTitle);
		TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
		current.setTextColor(ta.getColor(0,Theme.color));
		ta.recycle();
		setEnabled(enable);
		setMax(max);
		super.onBindView(view);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
	{
		// TODO: Implement this method
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		if(defaultValue!=null)
			index=getPersistedInt(defaultValue);
			else
		index=getPersistedInt(0);
	}
	public void setMax(int max){
		this.max=max;
		if(sb!=null){
		sb.setMax(max);
		notifyCustomChanged();
		}
	}
	public void setProgress(int progress){
		sb.setProgress(progress);
	}

	@Override
	public void setDefaultValue(Object defaultValue)
	{
		if(index==-1)index=defaultValue;
		super.setDefaultValue(defaultValue);
	}


	@Override
	public void setEnabled(boolean enabled)
	{
			this.enable=enabled;
			//if(sb!=null)
		//sb.setEnabled(enable);
		super.setEnabled(enabled);
	}

	
	
	@Override
	public void onStartTrackingTouch(SeekBar p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void onStopTrackingTouch(SeekBar p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void onProgressChanged(SeekBar p1, int p2, boolean p3)
	{
		index=p2;
		notifyCustomChanged();
		if(p3)persistInt(p2);
	}
public void add(int add){
	this.add=add;
	notifyCustomChanged();
}
public void notifyCustomChanged(){
	start.setText(0+add+"");
	current.setText(sb.getProgress()+add+"");
	end.setText(sb.getMax()+add+"");
}


}
