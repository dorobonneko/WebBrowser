package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.content.Context;
import java.util.List;
import android.view.View;
import com.moe.bean.SubSiteBean;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.moe.Mbrowser.R;
import android.support.v7.widget.GridLayoutManager;
import android.support.v4.widget.CircleImageView;
import android.graphics.Color;
import android.view.Gravity;
import com.moe.dialog.AddDialog;
import android.app.Dialog;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Message;
import java.util.LinkedList;
import android.content.DialogInterface;

public class IconAdapter extends RecyclerView.Adapter implements View.OnClickListener,AddDialog.OnDismissListener
{
    private Context context;
    private LinkedList<SubSiteBean> ls;
    private AddDialog ad;
    public IconAdapter(Context context,LinkedList<SubSiteBean> list){
        this.context=context;
        this.ls=list;
        ad=new AddDialog(context);
        ad.setOnDismissListener(this);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
    {
        LinearLayout ll=new LinearLayout(context);
        GridLayoutManager.LayoutParams gl=new GridLayoutManager.LayoutParams(GridLayoutManager.LayoutParams.WRAP_CONTENT,GridLayoutManager.LayoutParams.WRAP_CONTENT);
        gl.setMargins(20,20,20,20);
        ll.setLayoutParams(gl);
        ll.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.round_dialog));
        //ll.setPadding(20,20,20,20);
        //CircleImageView civ=new CircleImageView(context,0xffff00ff,1.5f);
        ImageView iv=new ImageView(context);
        TextView tv=new TextView(context);
        //iv.setBackground(context.getResources().getDrawable(R.drawable.round));
        ll.setOrientation(ll.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        ll.addView(iv);
        ll.addView(tv);
        ViewHolder vh=new ViewHolder(ll);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder p1, int p2)
    {
        //p1.itemView.setBackgroundColor(0xffff00ff);
        p1.itemView.setTag(p2);
        p1.itemView.setOnClickListener(this);
        ((ViewHolder)p1).tv.setText(ls.get(p2).getName());
        int id=ls.get(p2).getUrl().equals("add")?R.drawable.icon_add:R.drawable.ic_launcher;
        ((ViewHolder)p1).iv.setImageResource(id);
    }

    @Override
    public int getItemCount()
    {
        // TODO: Implement this method
        return ls.size();
    }

    @Override
    public void onClick(View p1)
    {
        if(ls.get(((Integer)p1.getTag()).intValue()).getUrl().equals("add")){
            
            ad.show();
            
        }
    }
public void refreshData(){
    new Thread(){
        public void run(){
            ls.clear();
            //ls.addAll();
            ls.addLast(new SubSiteBean(context.getResources().getString(R.string.add),"add"));
            handler.sendEmptyMessage(0);
        }
    }.start();
   
    
   
}

@Override
public void onDismiss(DialogInterface p1)
{
    refreshData();
}


    Handler handler=new Handler(){

        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case 0:
                    notifyDataSetChanged();
                    break;
            }
        }
        
    };
    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv;
        TextView tv;
        public ViewHolder(ViewGroup view){
            super(view);
            iv=(ImageView)view.getChildAt(0);
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            iv.setMinimumWidth(0);
            //iv.setBackgroundColor(0xffff0000);
            tv=(TextView)view.getChildAt(1);
            tv.setGravity(Gravity.CENTER);
        }
    }
}
