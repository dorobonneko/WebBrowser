package com.moe.adapter;
import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import java.util.ArrayList;
import java.util.MissingFormatArgumentException;
import android.widget.TextView;
import android.widget.ProgressBar;
import java.util.*;
import de.greenrobot.event.*;
import com.moe.bean.*;
import com.moe.fragment.*;
import com.moe.widget.*;
import android.support.v7.widget.helper.ItemTouchHelper;
import com.moe.webkit.WebViewManagerView;

public class WinListAdapter extends RecyclerView.Adapter<WinListAdapter.ViewHolder>
{
    private OnItemClickListener oicl;
    private ViewFlipper vf;
    private Context context;
    private int position=0;
    public void selected(int index)
    {
        position = index;
		notifyDataSetChanged();
    }
	public void setOnItemClickListener(OnItemClickListener o)
	{
		oicl = o;
	}
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup p1, int p2)
    {
        View v=LayoutInflater.from(context).inflate(R.layout.win_list_item, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, final int p2)
    {

        if (position == p2)
            vh.itemView.setBackgroundResource(R.drawable.win_item_selected);
		else
			vh.itemView.setBackgroundResource(R.drawable.win_item_normal);
		vh.setTitle(((WebViewManagerView)vf.getChildAt(p2)).getTitle());
		vh.setUrl(((WebViewManagerView)vf.getChildAt(p2)).getUrl());
		vh.state.setState(((WebViewManagerView)vf.getChildAt(p2)).getState());
    }

    @Override
    public int getItemCount()
    {
        return vf.getChildCount();
    }



    public WinListAdapter(Context context, ViewFlipper vf)
	{
        this.context = context;
		this.vf = vf;
    }
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView title,url;
        public RotateImageView state;
        private View close,click;
        public void setTitle(String title)
		{
            if (title == null || title.equals(""))
                this.title.setText(context.getResources().getString(R.string.new_win));
			else
				this.title.setText(title);
        }
        public void setUrl(String url)
		{
            this.url.setText(url);
        }
        public ViewHolder(View view)
		{
            super(view);
			view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, (int)context.getResources().getDimension(R.dimen.actionBarSize)));
            title = (TextView)view.findViewById(R.id.winlistitem_title);
            url = (TextView)view.findViewById(R.id.winlistitem_url);
            state = (RotateImageView)view.findViewById(R.id.winlistitem_state);
            close = view.findViewById(R.id.winlistitem_close);
			click = view.findViewById(R.id.winlistitem_click);
            //pb.setIndeterminate(true);
 			title.setText(context.getResources().getString(R.string.new_win));
			close.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						if (oicl != null)
							oicl.onItemClick(getPosition());
					}
				});
			click.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View p1)
					{
						EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_TOGGLE_WINDOW, getPosition()));
						EventBus.getDefault().post(WindowFragment.CLOSE);
					}
				});
        }
    }
    public abstract interface OnItemClickListener
	{
        void onItemClick(int index);
    }
}
