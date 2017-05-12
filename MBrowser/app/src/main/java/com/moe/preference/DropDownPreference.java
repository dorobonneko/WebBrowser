package com.moe.preference;
import android.preference.Preference;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.content.res.TypedArray;
import android.widget.ArrayAdapter;
import com.moe.Mbrowser.R;
import com.moe.database.Download;
import android.widget.AdapterView;
import android.widget.Adapter;
import java.util.HashMap;

public class DropDownPreference extends Preference implements Spinner.OnItemSelectedListener
{
	private Spinner spinner;
	private final static String xmlns="http://schemas.android.com/apk/res/android";
	private CharSequence[] array;
	//private CharSequence[] value;
	private int index=0;
	//private HashMap<Integer,Integer> hm=new HashMap();
	public DropDownPreference(Context context){
		super(context,null);
	}
	public DropDownPreference(Context context,AttributeSet attr){
		super(context,attr);
		init(attr);
	}
	public DropDownPreference(Context context,AttributeSet attr,int style){
		super(context,attr,style);
		init(attr);
	}
	public DropDownPreference(Context context,AttributeSet attrs,int style,int s){
		super(context,attrs,style,s);
		init(attrs);
		
	}
	private void init(AttributeSet attr){
		int array=attr.getAttributeResourceValue(xmlns,"entries",-1);
		index =attr.getAttributeIntValue(xmlns,"defaultValue",0);
		//int value=attr.getAttributeResourceValue(xmlns,"value",-1);
		if(array!=-1)
			this.array=getContext().getResources().getTextArray(array);
	/*	if(value!=-1)
			this.value=getContext().getResources().getTextArray(value);
			if(this.value!=null){
				for(CharSequence cs:this.value){
					hm.put(Integer.parseInt(cs.toString()),hm.size());
				}
			}*/
	}
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		ViewGroup v=(ViewGroup)super.onCreateView(parent);
		spinner=new Spinner(getContext());
		spinner.setMinimumWidth((int)getContext().getResources().getDimension(R.dimen.actionBarSize));
		((ViewGroup)v.getChildAt(2)).addView(spinner);
		//v.getChildAt(2).setBackgroundColor(0xff000000);
		v.getChildAt(2).setVisibility(View.VISIBLE);
		return v;
	}

	@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);
		if(array!=null)
		spinner.setAdapter(new ArrayAdapter(getContext(),android.R.layout.simple_spinner_dropdown_item,array));
		spinner.setOnItemSelectedListener(this);
		spinner.setSelection(index);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue)
	{
		super.onSetInitialValue(restorePersistedValue, defaultValue);
		index=getPersistedInt(index);
	}

	@Override
	public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
	{
		index=p3;
		persistInt(p3);
	}

	@Override
	public void onNothingSelected(AdapterView<?> p1)
	{
		// TODO: Implement this method
	}


	
}
