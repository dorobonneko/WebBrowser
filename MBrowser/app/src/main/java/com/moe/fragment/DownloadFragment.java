package com.moe.fragment;
import android.view.View;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.moe.Mbrowser.R;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.ImageView;
import com.moe.entity.TaskInfo;
import java.util.ArrayList;
import java.util.List;
import android.support.v7.widget.DefaultItemAnimator;
import com.moe.adapter.DownloadItemAdapter;
import com.moe.utils.CustomDecoration;
import com.moe.database.Download;
import com.moe.database.DataBase;
import com.moe.Mbrowser.ResourceService;
import com.moe.Mbrowser.DownloadService;
import java.util.Iterator;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.EventBus;
import com.moe.bean.TaskBean;
import de.greenrobot.event.ThreadMode;
import com.moe.download.DownloadTask;
import com.moe.bean.Message;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import java.net.MalformedURLException;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import java.net.URL;

public class DownloadFragment extends Fragment implements DownloadItemAdapter.OnClickListener
{
	private RecyclerView rv;
	private int currentPosition=0;
	private ImageView header;
	private List<TaskInfo> l1=new ArrayList<>(),l2=new ArrayList<>();
	private DownloadItemAdapter dia;
	private Download download;
	private long time;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v=inflater.inflate(R.layout.download_view, container, false);
		header=(ImageView)v.findViewById(R.id.download_view_header);
		rv = (RecyclerView)v.findViewById(R.id.download_view_recyclerview);
		final LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
		rv.setLayoutManager(linearLayoutManager);
		rv.addOnScrollListener(new RecyclerView.OnScrollListener(){
				
				@Override
				public void onScrolled(RecyclerView recyclerView, int dx, int dy)
				{
					super.onScrolled(recyclerView, dx, dy);
					View view=linearLayoutManager.findViewByPosition(currentPosition + 1);
					if (view == null)return;
				}
			});
		rv.addItemDecoration(new CustomDecoration(1));
		//rv.setItemAnimator(new DefaultItemAnimator());
		rv.setAdapter(dia=new DownloadItemAdapter(l1,l2));
		dia.notifyDataSetChanged();
		dia.setOnClickListener(this);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		download=DataBase.getInstance(getContext());
		refresh();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onClick(int position)
	{
		if(position<=l1.size()){
			TaskInfo ti=l1.get(position-1);
			if(ti.isDownloading())
			EventBus.getDefault().post(new TaskBean(ti,TaskBean.State.PAUSE));
				else
			EventBus.getDefault().post(new TaskBean(ti,TaskBean.State.ADD));
		}else{
			TaskInfo ti=l2.get(position-l1.size()-2);
			Intent intent = new Intent(Intent.ACTION_VIEW);
//判断是否是AndroidN以及更高的版本
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				Uri contentUri = FileProvider.getUriForFile(getActivity(), getContext().getPackageName() + ".fileProvider",new File(ti.getDir(), ti.getTaskname()));
				intent.setDataAndType(contentUri,ti.getType());
				
			} else {
				intent.setDataAndType(Uri.fromFile(new File(ti.getDir(), ti.getTaskname())),ti.getType());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			try{startActivity(intent);}catch(Exception e){
				intent.setDataAndType(intent.getData(),MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl((intent.getDataString()))));
				try{startActivity(intent);}catch(Exception e2){}
			}
			}
	}


	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if(!hidden){
			refresh();
					}
		super.onHiddenChanged(hidden);
	}
	public synchronized void refresh(){
		//rv.setAdapter(dia);
		l1.clear();l2.clear();
		dia.notifyDataSetChanged();
		if(ResourceService.isRunning(getContext())){
		List<TaskInfo> lti=download.getAllTaskInfoWithState(false);
		for (int i=0;i<lti.size();i++){
			TaskInfo ti=lti.get(i);
			if(DownloadService.downloadlist.containsKey(ti.getId())){
				lti.remove(i);
				lti.add(i,DownloadService.downloadlist.get(ti.getId()));
			}
		}
		l1.addAll(lti);
		}else{
		l1.addAll(download.getAllTaskInfoWithState(false));}
		l2.addAll(download.getAllTaskInfoWithState(true));
		dia.notifyDataSetChanged();
		
	}
	@Subscribe(threadMode = ThreadMode.MainThread)
	public synchronized void updateTaskInfo(TaskInfo ti){
		if(!ti.isDownloading()||System.currentTimeMillis()-time>1000){
		if(!l1.contains(ti))refresh();
		if(l1.contains(ti)){
			dia.notifyItemChanged(l1.indexOf(ti)+1);
			//dia.notifyDataSetChanged();
			}
		time=System.currentTimeMillis();
		}
	}
	@Subscribe
	public void message(Message msg){
		if(msg.what==88888)refresh();
	}
	@Override
	public boolean onBackPressed()
	{
		// TODO: Implement this method
		return false;
	}

	@Override
	public void onDetach()
	{
		EventBus.getDefault().unregister(this);
		super.onDetach();
	}

}
