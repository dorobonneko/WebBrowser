package com.moe.fragment;
import android.os.Bundle;
import android.view.View;
import com.moe.internal.Theme;

public abstract class Fragment extends android.support.v4.app.Fragment
{

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		view.setOnClickListener(null);
		Theme.registerBackground(view);
		super.onViewCreated(view, savedInstanceState);
	}

    
      public abstract boolean onBackPressed();

   
}
