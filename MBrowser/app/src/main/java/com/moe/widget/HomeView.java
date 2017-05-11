package com.moe.widget;
import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import com.moe.adapter.IconAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import java.util.LinkedList;
import com.moe.bean.SubSiteBean;

public class HomeView extends RecyclerView
{
    private IconAdapter ia;
   
    private LinkedList<SubSiteBean> iconlist=new LinkedList<>();
    
    public HomeView(Context context){
        super(context);
        
        setLayoutManager(new GridLayoutManager(context, 4));
        setAdapter(ia = new IconAdapter(context, iconlist));
        setItemAnimator(new DefaultItemAnimator());
        setNestedScrollingEnabled(true);
        ia.refreshData();
    }
    
 
    
}
