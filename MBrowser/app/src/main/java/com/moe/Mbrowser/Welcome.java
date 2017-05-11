package com.moe.Mbrowser;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.moe.fragment.Fragment;
import com.moe.fragment.MainFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.CoordinatorLayout;
import android.view.ViewGroup;
import com.moe.fragment.TestFragment;
import android.widget.LinearLayout;
import android.app.Activity;

public class Welcome extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // TODO: Implement this method
        super.onCreate(savedInstanceState);
        LinearLayout c=new LinearLayout(this);
        c.setId(5555);
        setContentView(c,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        c.setBackgroundColor(0xffff0000);
        getFragmentManager().beginTransaction().add(5555,new TestFragment()).commit();
    }

    }
  
