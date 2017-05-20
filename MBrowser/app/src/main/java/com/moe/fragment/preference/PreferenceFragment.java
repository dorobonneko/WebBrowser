package com.moe.fragment.preference;
import android.os.Bundle;
import android.view.View;
import android.content.res.TypedArray;
import com.moe.Mbrowser.R;

public abstract class PreferenceFragment extends android.preference.PreferenceFragment
{

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.colorPrimaryDark});
		view.setBackgroundColor(ta.getColor(0,R.color.primary_dark));
		ta.recycle();
		super.onViewCreated(view, savedInstanceState);
	}
	
	public abstract boolean onBackPressed();
}
