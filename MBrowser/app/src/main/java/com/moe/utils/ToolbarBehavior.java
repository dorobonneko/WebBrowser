package com.moe.utils;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.support.v4.view.ViewCompat;
import com.moe.Mbrowser.R;
import java.util.ArrayList;

public class ToolbarBehavior extends CoordinatorLayout.Behavior<View>
{

	private float top;
	private int state=1;
	public ToolbarBehavior(Context context,AttributeSet attrs){
		super(context,attrs);
	}

	@Override
	public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes)
	{
		top=child.getHeight();
		return nestedScrollAxes==ViewCompat.SCROLL_AXIS_VERTICAL;
	}
	@Override
	public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed)
	{
		super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
		/**if(dy>0){//上滑
			if(oldy-dy>-top){
				child.offsetTopAndBottom(dy-y);
			}else{
				child.setY(-top);
			}
		}else{
			if(dy-oldy>0){
				child.offsetTopAndBottom(y-dy);
			}else{
				child.setY(0);
			}
		}*/
		if(Math.abs(dy)<top){
			if(dy>0){
		if(state==1)child.setY(-dy);}
		else{
		if(state==0)child.setY(-top-dy);}
		}
		else{
			if(dy>0){
			child.setY(-top);
			state=0;
			}else{
			child.setY(0);
			state=1;
			}
		}
	}

	@Override
	public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target)
	{
		if(child.getY()<0&&child.getY()>-top){
			if(child.getY()>-top/2){
				coordinatorLayout.findViewById(R.id.main_content).setPadding(0,(int)top,0,(int)top);
				child.setY(0);
			}else{
				coordinatorLayout.findViewById(R.id.main_content).setPadding(0,0,0,(int)top);
				child.setY(-top);
				}
		}
		super.onStopNestedScroll(coordinatorLayout, child, target);
	}

	

}
