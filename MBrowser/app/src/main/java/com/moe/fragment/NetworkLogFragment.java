package com.moe.fragment;
import android.view.View;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.moe.Mbrowser.R;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import java.util.LinkedHashMap;
import com.moe.utils.LinkedListMap;
import com.moe.adapter.ViewPagerAdapter;
import java.util.List;
import java.util.ArrayList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.internal.CustomDecoration;
import com.moe.adapter.NetworkLogAdapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import com.moe.fragment.NetworkLogFragment.Type;
import de.greenrobot.event.EventBus;
import com.moe.bean.WindowEvent;
import android.widget.Toast;
import android.text.ClipboardManager;
import android.app.Service;

public class NetworkLogFragment extends Fragment implements NetworkLogAdapter.OnItemClickListener,NetworkLogAdapter.OnItemLongClickListener
{
private LinkedListMap<Type,List<String>> llm=new LinkedListMap<>();
private List<RecyclerView> lv;
private RecyclerView image,audio,video,css,js,other;
private ViewPagerAdapter vpa;
private TabLayout tl;
private ClipboardManager cm;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		image=new RecyclerView(container.getContext());
		audio=new RecyclerView(container.getContext());
		video=new RecyclerView(container.getContext());
		css=new RecyclerView(container.getContext());
		js=new RecyclerView(container.getContext());
		other=new RecyclerView(container.getContext());
		audio.setLayoutManager(new LinearLayoutManager(container.getContext()));
		image.setLayoutManager(new LinearLayoutManager(container.getContext()));
		video.setLayoutManager(new LinearLayoutManager(container.getContext()));
		css.setLayoutManager(new LinearLayoutManager(container.getContext()));
		js.setLayoutManager(new LinearLayoutManager(container.getContext()));
		other.setLayoutManager(new LinearLayoutManager(container.getContext()));
		audio.addItemDecoration(new CustomDecoration(1));
		video.addItemDecoration(new CustomDecoration(1));
		css.addItemDecoration(new CustomDecoration(1));
		js.addItemDecoration(new CustomDecoration(1));
		other.addItemDecoration(new CustomDecoration(1));
		image.addItemDecoration(new CustomDecoration(1));
		audio.setAdapter(new NetworkLogAdapter(llm,Type.AUDIO,this));	
		video.setAdapter(new NetworkLogAdapter(llm,Type.VIDEO,this));	
		css.setAdapter(new NetworkLogAdapter(llm,Type.CSS,this));	
		js.setAdapter(new NetworkLogAdapter(llm,Type.JS,this));	
		other.setAdapter(new NetworkLogAdapter(llm,Type.OTHER,this));
		image.setAdapter(new NetworkLogAdapter(llm,Type.IMAGE,this));
		audio.setTag("音频");
		video.setTag("视频");
		css.setTag("CSS");
		js.setTag("JAVASCRIPT");
		other.setTag("其它");
		image.setTag("图片");
		return inflater.inflate(R.layout.networklog_view,container,false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		tl=(TabLayout)view.findViewById(R.id.networklog_view_tablayout);
		ViewPager vp=(ViewPager)view.findViewById(R.id.networklog_view_viewpager);
		vp.setAdapter(vpa=new ViewPagerAdapter(lv=new ArrayList<>()));
		lv.add(audio);
		lv.add(video);
		lv.add(image);
		lv.add(css);
		lv.add(js);
		lv.add(other);
		vpa.notifyDataSetChanged();
		tl.setupWithViewPager(vp);
		tl.setTabsFromPagerAdapter(vpa);
		tl.setTabMode(TabLayout.MODE_SCROLLABLE);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		cm=(ClipboardManager)getActivity().getSystemService(Service.CLIPBOARD_SERVICE);
		super.onActivityCreated(savedInstanceState);
		initData();
	}
	
	public void setArguments(LinkedListMap<Type,List<String>> args)
	{
		llm.clear();
		llm.put(Type.AUDIO,args.getKey(Type.AUDIO));
		llm.put(Type.VIDEO,args.getKey(Type.VIDEO));
		llm.put(Type.CSS,args.getKey(Type.CSS));
		llm.put(Type.JS,args.getKey(Type.JS));
		llm.put(Type.OTHER,args.getKey(Type.OTHER));
		llm.put(Type.IMAGE,args.getKey(Type.IMAGE));
		if(isAdded())
			initData();
	}
	private void changetTab(Type type,int position,String title){
		List<String> ls=llm.getKey(type);
		if(ls!=null&&ls.size()>0)
			tl.getTabAt(position).setText(title+"("+ls.size()+")");
			else
			tl.getTabAt(position).setText(title);
	}
	private void initData(){
		//lv.clear();
		changetTab(Type.AUDIO,0,"音频");
		changetTab(Type.VIDEO,1,"视频");
		changetTab(Type.IMAGE,2,"图片");
		changetTab(Type.CSS,3,"CSS");
		changetTab(Type.JS,4,"JAVASCRIPT");
		changetTab(Type.OTHER,5,"其它");
		
		for(RecyclerView rv:lv)
		rv.getAdapter().notifyDataSetChanged();
	}
	@Override
	public void onItemClick(NetworkLogFragment.Type type, RecyclerView.ViewHolder vh)
	{
		EventBus.getDefault().post(new WindowEvent(WindowEvent.WHAT_URL_WINDOW,llm.getKey(type).get(vh.getAdapterPosition())));
		getActivity().onBackPressed();
	}

	@Override
	public boolean onItemLongClick(NetworkLogFragment.Type type, RecyclerView.ViewHolder vh)
	{
		cm.setText(llm.getKey(type).get(vh.getAdapterPosition()));
		Toast.makeText(getActivity(),"已复制",Toast.LENGTH_SHORT).show();
		return true;
	}



	

	@Override
	public boolean onBackPressed()
	{
		return false;
	}
	public enum Type{
		AUDIO,VIDEO,IMAGE,CSS,JS,OTHER
	}
	
}
