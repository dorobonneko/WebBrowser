package com.moe.fragment;
import android.os.Bundle;
import com.moe.widget.ViewFlipper;
import android.view.animation.Animation;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.Animator;
import android.view.animation.AnimationUtils;

public abstract class FragmentPop extends Fragment
{private OnHideListener ohl;

    
   @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onActivityCreated(savedInstanceState);
        if(ohl!=null)
            ohl.hide(this,false);
   }

   @Override
   public void onHiddenChanged(boolean hidden)
   {
       if(!hidden)
           if(ohl!=null)
               ohl.hide(this,false);
       super.onHiddenChanged(hidden);
   }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim)
    {
		
        if(!enter&&nextAnim!=0){
        Animation anim= AnimationUtils.loadAnimation(getActivity(),nextAnim);
        anim.setAnimationListener(new Animation.AnimationListener(){

                @Override
                public void onAnimationStart(Animation p1)
                {
                    // TODO: Implement this method
                }

                @Override
                public void onAnimationEnd(Animation p1)
                {if(ohl!=null)
						ohl.hide(FragmentPop.this,true);
                      }

                @Override
                public void onAnimationRepeat(Animation p1)
                {
                    // TODO: Implement this method
                }
            });
      return anim;
        }else if(!enter){
			if(ohl!=null)
				ohl.hide(FragmentPop.this,true);
			
		}
        return super.onCreateAnimation(transit,enter,nextAnim);
    }
    
    public void setOnHideListener(OnHideListener ohl){
        if(ohl!=null)
            this.ohl=ohl;
    }
    public abstract interface OnHideListener{
        void hide(FragmentPop f,boolean ishide);
    }
}
