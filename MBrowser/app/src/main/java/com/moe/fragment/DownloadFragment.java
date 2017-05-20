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
import android.widget.ViewFlipper;
import android.widget.Button;
import android.util.TypedValue;
import android.content.res.TypedArray;
import com.moe.bean.DownloadItem;
import com.moe.dialog.BottomDialog;
import android.content.DialogInterface;
import android.support.v7.widget.SimpleItemAnimator;
import android.content.ClipboardManager;
import com.moe.dialog.DownloadNewDialog;

public class DownloadFragment extends Fragment implements DownloadItemAdapter.OnClickListener,DownloadItemAdapter.OnLongClickListener,View.OnClickListener
{
	private RecyclerView rv;
	private int currentPosition=0;
	private ImageView header;
	private List<TaskInfo> l1=new ArrayList<>(),l2=new ArrayList<>();
	private DownloadItemAdapter dia;
	private Download download;
	private long time;
	//是否编辑模式
	private boolean edit=false;
	private ViewFlipper toggle;
	private Button delete,redownload,more;
	private ClipboardManager cm;
	private ArrayList<Integer> selected=new ArrayList<Integer>(){
		@Override
		public boolean add(Integer object)
		{
			boolean f= super.add(object);
			check();
			return f;
		}
		@Override
		public Integer remove(int index)
		{
			Integer i= super.remove(index);
			check();
			return i;
		}

		@Override
		public boolean remove(Object object)
		{
			boolean f= super.remove(object);
			check();
			return f;
		}
		
		@Override
		public void clear()
		{
			super.clear();
			check();
		}
		
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v=inflater.inflate(R.layout.download_view, container, false);
				return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.colorPrimaryDark});
		view.setBackgroundColor(ta.getColor(0,R.color.primary_dark));
		ta.recycle();
		header = (ImageView)view.findViewById(R.id.download_view_header);
		rv = (RecyclerView)view.findViewById(R.id.download_view_recyclerview);
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
		rv.setAdapter(dia = new DownloadItemAdapter(l1, l2,selected));
		dia.notifyDataSetChanged();
		dia.setOnClickListener(this);
		dia.setOnLongClickListener(this);
		toggle=(ViewFlipper)view.findViewById(R.id.download_view_toggle);
		view.findViewById(R.id.download_view_clear).setOnClickListener(this);
		view.findViewById(R.id.download_view_new).setOnClickListener(this);
		view.findViewById(R.id.download_view_edit).setOnClickListener(this);
		redownload=(Button)view.findViewById(R.id.download_view_redownload);
		redownload.setOnClickListener(this);
		more=(Button)view.findViewById(R.id.download_view_more);
		more.setOnClickListener(this);
		view.findViewById(R.id.download_view_cancel).setOnClickListener(this);
		delete=(Button)view.findViewById(R.id.download_view_delete);
		delete.setOnClickListener(this);
		toggle.setInAnimation(getActivity(),R.anim.bottom_up);
		toggle.setOutAnimation(getActivity(),R.anim.up_up);
		//关闭动画避免闪烁
		((SimpleItemAnimator)rv.getItemAnimator()).setSupportsChangeAnimations(false);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		cm=getContext().getSystemService(ClipboardManager.class);
		download = DataBase.getInstance(getContext());
		refresh();
		EventBus.getDefault().register(this);
	}
	public void check(){
		if(selected.size()==1)more.setEnabled(true);else more.setEnabled(false);
		if(selected.size()>0){redownload.setEnabled(true);delete.setEnabled(true);}else{redownload.setEnabled(false);delete.setEnabled(false);}
	}
	@Override
	public void onClick(int position)
	{
		if (edit)
		{
			if(position!=0&&position!=l1.size()+1){
				
				if(selected.contains(position))
					selected.remove((Integer)position);
					else
					selected.add(position);
					dia.notifyItemChanged(position);
			}
		}
		else
		{
			if (position <= l1.size())
			{
				TaskInfo ti=l1.get(position - 1);
				if (ti.isDownloading())
					EventBus.getDefault().post(new TaskBean(ti, TaskBean.State.PAUSE));
				else
					EventBus.getDefault().post(new TaskBean(ti, TaskBean.State.ADD));
			}
			else
			{
				TaskInfo ti=l2.get(position - l1.size() - 2);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					Uri contentUri = FileProvider.getUriForFile(getActivity(), getContext().getPackageName() + ".fileProvider", new File(ti.getDir(), ti.getTaskname()));
					intent.setDataAndType(contentUri, ti.getType());

				}
				else
				{
					intent.setDataAndType(Uri.fromFile(new File(ti.getDir(), ti.getTaskname())), ti.getType());
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				}
				try
				{startActivity(intent);}
				catch (Exception e)
				{
					intent.setDataAndType(intent.getData(), MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl((intent.getDataString()))));
					try
					{startActivity(intent);}
					catch (Exception e2)
					{}
				}
			}
		}
	}

	@Override
	public boolean onLongClick(int position)
	{
		if(position!=0&&position!=l1.size()+1&&!edit){
			edit=true;
			toggle.setDisplayedChild(1);
			selected.add(position);
			dia.notifyItemChanged(position);
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View p1)
	{
		switch(p1.getId()){
			case R.id.download_view_redownload://重新下载
			new BottomDialog.Builder(getActivity())
					.addArrayItem(new String[]{"重新下载","取消"}, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							switch(p2){
								case 0:
									TaskInfo ti = null;
									for(Integer i:selected){
										switch(dia.getItemViewType(i)){
											case 2:
												ti=dia.getItem(i);
												break;
											case 1:
												ti=dia.getItem(i);
												EventBus.getDefault().post(new TaskBean(ti, TaskBean.State.PAUSE));
												break;
										}
										download.deleteTaskInfoWithId(ti.getId());
										ti.setSuccess(false);
										ti.setDownloadinfo(null);
										//download.deleteDownloadInfoWithId(ti.getId());
										download.addTaskInfo(ti);
									}
									selected.clear();
									refresh();
									break;
							}
							p1.dismiss();
						}
					}).show();
			break;
			case R.id.download_view_new://新建任务
				new DownloadNewDialog(getActivity(), new DownloadNewDialog.Callback(){

						@Override
						public void Added(TaskInfo ti)
						{
							l1.add(ti);
							dia.notifyItemInserted(l1.size());
						}
					}).show();
			break;
			case R.id.download_view_edit://编辑模式
			edit=true;
			toggle.setDisplayedChild(1);
			check();
			break;
			case R.id.download_view_clear://清空任务
			if(l2.size()>0)
				new BottomDialog.Builder(getActivity())
						.addArrayItem(new String[]{"清空任务","清空任务(和文件一起)","取消"}, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							int size;
							switch(p2){
								case 0:
									download.clearAllSuccessTask(false);
									size=l2.size();
									l2.clear();
									dia.notifyItemRangeRemoved(l1.size()+2,size);
									break;
								case 1:
									download.clearAllSuccessTask(true);
									size=l2.size();
									l2.clear();
									dia.notifyItemRangeRemoved(l1.size()+2,size);
									
									break;
							}
							p1.dismiss();
											p1.dismiss();
						}
					}).show();
			break;
			case R.id.download_view_delete://删除任务
				new BottomDialog.Builder(getActivity())
					.addArrayItem(new String[]{"删除已选任务","删除任务(和文件一起)","取消"}, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							ArrayList<TaskInfo> ati;
							switch(p2){
								case 0:
									ati=new ArrayList<>();
									for(Integer i:selected){
										switch(dia.getItemViewType(i)){
											case 2:
												ati.add(dia.getItem(i));
												break;
											case 1:
												ati.add(dia.getItem(i));
												EventBus.getDefault().post(new TaskBean(l1.get(i-1), TaskBean.State.PAUSE));
												break;
										}
											
									}
									download.clearAllTask(ati.toArray(),false);
									selected.clear();
									refresh();
									break;
								case 1:
									ati=new ArrayList<>();
									for(Integer i:selected){
										switch(dia.getItemViewType(i)){
											case 2:
												ati.add(dia.getItem(i));
												break;
											case 1:
												ati.add(dia.getItem(i));
												EventBus.getDefault().post(new TaskBean(l1.get(i-1), TaskBean.State.PAUSE));
												break;
										}
										
									}
									download.clearAllTask(ati.toArray(),true);
									selected.clear();
									refresh();
									break;
							}
							p1.dismiss();
						}
					}).show();
			break;
			case R.id.download_view_more://更多
				new BottomDialog.Builder(getActivity())
					.addArrayItem(new String[]{"任务详情","复制下载链接","取消"}, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							switch(p2){
								case 0:
									break;
								case 1:
									cm.setText(((TaskInfo)dia.getItem(selected.get(0))).getTaskurl());
									break;
							}
							p1.dismiss();
						}
					}).show();
			break;
			case R.id.download_view_cancel:
				cancel();
			break;
		}
	}

	private void cancel(){
		edit=false;
		toggle.setDisplayedChild(0);
		selected.clear();
		dia.notifyDataSetChanged();
	}

	@Override
	public void onHiddenChanged(boolean hidden)
	{
		if (!hidden)
		{
			refresh();
		}
		super.onHiddenChanged(hidden);
	}
	public synchronized void refresh()
	{
		//rv.setAdapter(dia);
		if (ResourceService.isRunning(getContext()))
		{
			List<TaskInfo> lti=download.getAllTaskInfoWithState(false);
			for (int i=0;i < lti.size();i++)
			{
				TaskInfo ti=lti.get(i);
				if (DownloadService.downloadlist.containsKey(ti.getId()))
				{
					lti.remove(i);
					lti.add(i, DownloadService.downloadlist.get(ti.getId()));
				}
			}
			l1.clear();
			l1.addAll(lti);
		}
		else
		{
			l1.clear();
			l1.addAll(download.getAllTaskInfoWithState(false));
			}
		l2.clear();
		l2.addAll(download.getAllTaskInfoWithState(true));
		dia.notifyDataSetChanged();

	}
	@Subscribe(threadMode = ThreadMode.MainThread)
	public synchronized void updateTaskInfo(TaskInfo ti)
	{
		if(ti.isSuccess()){
			int sead=l1.indexOf(ti);
			l1.remove(ti);
			dia.notifyItemRemoved(sead+1);
			l2.add(0,ti);
			dia.notifyItemInserted(l1.size()+2);
			return;
		}
		if(ti.isDownloading()){
		if (System.currentTimeMillis() - time > 1000)
		{
			dia.notifyItemChanged(l1.indexOf(ti) + 1);
			time = System.currentTimeMillis();
		}
		}else{
			dia.notifyItemChanged(l1.indexOf(ti) + 1);
		}
	}
	@Subscribe
	public void message(Message msg)
	{
		/**if (msg.what == 88888)
			refresh();*/
	}
	@Override
	public boolean onBackPressed()
	{
		if(edit){
			cancel();
			return true;
		}
		return false;
	}

	@Override
	public void onDetach()
	{
		EventBus.getDefault().unregister(this);
		super.onDetach();
	}

}
