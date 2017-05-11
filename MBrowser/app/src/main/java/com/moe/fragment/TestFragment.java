package com.moe.fragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestFragment extends android.app.Fragment
{

    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        LinearLayout v=new LinearLayout(getActivity());
        TextView t=new TextView(getActivity());
        t.setText("vggg");
        //v.addView(t);
        v.setBackgroundColor(0xfff000ff);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        return v;
    }
    
}
