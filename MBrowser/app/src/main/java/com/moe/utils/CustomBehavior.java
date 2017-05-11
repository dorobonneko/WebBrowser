package com.moe.utils;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.util.AttributeSet;
import android.content.Context;
import com.moe.widget.ViewFlipper;
import com.moe.Mbrowser.R;
import android.support.v4.view.ViewCompat;
import java.util.ArrayList;
import android.inputmethodservice.Keyboard;

public class CustomBehavior extends CoordinatorLayout.Behavior<ViewFlipper>
{
	private float top;
	private boolean isfull=true;
public CustomBehavior(Context context,AttributeSet attr){
	super(context,attr);
	top=context.getResources().getDimension(R.dimen.actionBarSize);
	
}

@Override
public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, ViewFlipper child, View directTargetChild, View target, int nestedScrollAxes)
{
	return nestedScrollAxes==ViewCompat.SCROLL_AXIS_VERTICAL;
}

@Override
public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, ViewFlipper child, View target, int dx, int dy, int[] consumed)
{
	super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
	if(dy>0){//上滑
	if(isfull){
		if(top-dy>0)child.setPadding(0,(int)top-dy,0,0);
		else if(isfull){
			child.setPadding(0,0,0,(int)top);
			isfull=false;
		}}
	}else{
		if(!isfull){
		if(0-dy<top)child.setPadding(0,0-dy,0,0);
		else if(!isfull){
			child.setPadding(0,(int)top,0,(int)top);
			isfull=true;
		}
		}
	}
}

@Override
public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, ViewFlipper child, View target)
{
	super.onStopNestedScroll(coordinatorLayout, child, target);
}

	
}
