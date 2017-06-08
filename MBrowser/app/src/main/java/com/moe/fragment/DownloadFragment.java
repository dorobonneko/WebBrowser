package com.moe.fragment;
import de.greenrobot.event.Subscribe;
import com.moe.adapter.DownloadItemAdapter;
import android.view.View;
import com.moe.dialog.RenameDialog;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import com.moe.utils.LinkedListMap;
import com.moe.entity.TaskInfo;
import com.moe.database.Download;
import android.widget.ViewFlipper;
import android.widget.Button;
import android.content.ClipboardManager;
import com.moe.dialog.DownloadNewDialog;
import com.moe.dialog.BottomDialog;
import com.moe.dialog.TaskInfoDialog;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import com.moe.Mbrowser.R;
import android.support.v7.widget.LinearLayoutManager;
import com.moe.utils.CustomDecoration;
import android.support.v7.widget.SimpleItemAnimator;
import de.greenrobot.event.EventBus;
import com.moe.database.Sqlite;
import com.moe.bean.TaskBean;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.net.Uri;
import java.io.File;
import android.webkit.MimeTypeMap;
import android.content.DialogInterface;
import android.widget.Toast;
import java.util.Map;
import com.moe.Mbrowser.ResourceService;
import com.moe.Mbrowser.DownloadService;
import de.greenrobot.event.ThreadMode;

public class DownloadFragment extends Fragment implements DownloadItemAdapter.OnClickListener,DownloadItemAdapter.OnLongClickListener,View.OnClickListener,RenameDialog.Callback
{
	private RecyclerView rv;
	private int currentPosition=0;
	private ImageView header;
	private LinkedListMap<Integer,TaskInfo> loading,success;
	private DownloadItemAdapter dia;
	private Download download;
	//是否编辑模式
	private boolean edit=false;
	private ViewFlipper toggle;
	private Button delete,redownload,more;
	private ClipboardManager cm;
	private DownloadNewDialog dnd;
	private BottomDialog b_more;
	private RenameDialog rd;
	private TaskInfoDialog tid;
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
		loading = new LinkedListMap<>();
		success = new LinkedListMap<>();

		return inflater.inflate(R.layout.download_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		/**TypedArray ta=getContext().obtainStyledAttributes(new int[]{android.support.v7.appcompat.R.attr.colorPrimaryDark});
		 view.setBackgroundColor(ta.getColor(0,R.color.primary_dark));
		 ta.recycle();*/
		//Theme.registerBackground(view);
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
		rv.setAdapter(dia = new DownloadItemAdapter(loading, success, selected));
		dia.notifyDataSetChanged();
		dia.setOnClickListener(this);
		dia.setOnLongClickListener(this);
		toggle = (ViewFlipper)view.findViewById(R.id.download_view_toggle);
		view.findViewById(R.id.download_view_clear).setOnClickListener(this);
		view.findViewById(R.id.download_view_new).setOnClickListener(this);
		view.findViewById(R.id.download_view_edit).setOnClickListener(this);
		redownload = (Button)view.findViewById(R.id.download_view_redownload);
		redownload.setOnClickListener(this);
		more = (Button)view.findViewById(R.id.download_view_more);
		more.setOnClickListener(this);
		view.findViewById(R.id.download_view_cancel).setOnClickListener(this);
		delete = (Button)view.findViewById(R.id.download_view_delete);
		delete.setOnClickListener(this);
		toggle.setInAnimation(getActivity(), R.anim.bottom_up);
		toggle.setOutAnimation(getActivity(), R.anim.up_up);
		//关闭动画避免闪烁
		((SimpleItemAnimator)rv.getItemAnimator()).setSupportsChangeAnimations(false);
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		cm = getContext().getSystemService(ClipboardManager.class);
		download = Sqlite.getInstance(getContext(), Download.class);
		refresh();
		EventBus.getDefault().register(this);
	}
	public void check()
	{
		if (selected.size() == 1)more.setEnabled(true);else more.setEnabled(false);
		if (selected.size() > 0)
		{redownload.setEnabled(true);delete.setEnabled(true);}
		else
		{redownload.setEnabled(false);delete.setEnabled(false);}
	}
	@Override
	public void onClick(int position)
	{
		if (edit)
		{
			if (position != 0 && position != loading.size() + 1)
			{

				if (selected.contains(position))
					selected.remove((Integer)position);
				else
					selected.add(position);
				dia.notifyItemChanged(position);
			}
		}
		else
		{
			TaskInfo ti=dia.getItem(position);
			switch (dia.getItemViewType(position))
			{
				case 0:
					break;
				case 1:
					if (ti.isDownloading())
						EventBus.getDefault().post(new TaskBean(ti, TaskBean.State.PAUSE));
					else
						EventBus.getDefault().post(new TaskBean(ti, TaskBean.State.ADD));
					break;
				case 2:
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
					break;
			}

		}
	}

	@Override
	public boolean onLongClick(int position)
	{
		if (position != 0 && position != loading.size() + 1 && !edit)
		{
			edit = true;
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
		switch (p1.getId())
		{
			case R.id.download_view_redownload://重新下载
				new BottomDialog.Builder(getActivity())
					.addArrayItem(new String[]{"重新下载","取消"}, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface p1, int p2)
						{
							switch (p2)
							{
								case 0:
									final ArrayList<TaskInfo> at=new ArrayList<>();
									for (Integer i:selected)
										at.add(dia.getItem(i));
									for(TaskInfo ti:at){
										dia.delete(ti.getId());
										download.clearAllTask(new TaskInfo[]{ti}, true);
										ti.setSuccess(false);
										ti.setDownloadinfo(null);
										//download.deleteDownloadInfoWithId(ti.getId());
										download.addTaskInfo(ti, null);
									}
									at.clear();
									selected.clear();
									dia.notifyDataSetChanged();
									break;
							}
							p1.dismiss();
						}
					}).show();
				break;
			case R.id.download_view_new://新建任务
				if (dnd == null)
				{dnd = new DownloadNewDialog(getActivity(), new DownloadNewDialog.Callback(){

							@Override
							public void Added(final TaskInfo ti)
							{
								getView().post(new Runnable(){

										@Override
										public void run()
										{
											loading.put(ti.getId(), ti);
											dia.notifyItemInserted(loading.size());

										}
									});
							}
						});}
				dnd.show();
				break;
			case R.id.download_view_edit://编辑模式
				edit = true;
				toggle.setDisplayedChild(1);
				check();
				break;
			case R.id.download_view_clear://清空任务
				if (success.size() > 0)
					new BottomDialog.Builder(getActivity())
						.addArrayItem(new String[]{"清空任务","清空任务(和文件一起)","取消"}, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								int size;
								switch (p2)
								{
									case 0:
										download.clearAllSuccessTask(false);
										size = success.size();
										success.clear();
										dia.notifyItemRangeRemoved(loading.size() + 2, size);
										break;
									case 1:
										download.clearAllSuccessTask(true);
										size = success.size();
										success.clear();
										dia.notifyItemRangeRemoved(loading.size() + 2, size);

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
							ArrayList<TaskInfo> ati=new ArrayList<>();
							for (Integer i:selected)
							{
								switch (dia.getItemViewType(i))
								{
									case 2:
										ati.add(dia.getItem(i));
										break;
									case 1:
										ati.add(dia.getItem(i));
										//删除任务自动发送删除事件
										//EventBus.getDefault().post(new TaskBean(l1.get(i-1), TaskBean.State.STOP));
										break;
								}

							}
							switch (p2)
							{
								case 0:
									download.clearAllTask(ati.toArray(new TaskInfo[]{}), false);
									break;
								case 1:
									download.clearAllTask(ati.toArray(new TaskInfo[]{}), true);
									break;
							}
							selected.clear();
							refresh();
							p1.dismiss();
						}
					}).show();
				break;
			case R.id.download_view_more://更多
				if (b_more == null)
				{b_more = new BottomDialog.Builder(getActivity())
						.addArrayItem(new String[]{"任务详情","复制下载链接","重命名","取消"}, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								switch (p2)
								{
									case 0:
										if (tid == null)tid = new TaskInfoDialog(getActivity());
										tid.show(dia.getItem(selected.get(0)));
										break;
									case 1:
										cm.setText(dia.getItem(selected.get(0)).getTaskurl());
										break;
									case 2:
										if (dia.getItemViewType(selected.get(0)) == 1)
										{
											Toast.makeText(getActivity(), "正在下载的任务无法更改名称！", Toast.LENGTH_SHORT).show();
										}
										else
										{
											if (rd == null)
											{
												rd = new RenameDialog(getActivity());
											}
											rd.show(dia.getItem(selected.get(0)), DownloadFragment.this, selected.get(0).intValue());
										}break;
								}
								p1.dismiss();
							}
						}).create();}
				b_more.show();
				break;
			case R.id.download_view_cancel:
				cancel();
				break;
		}
	}

	@Override
	public void success(int position)
	{
		dia.notifyItemChanged(position);
	}


	private void cancel()
	{
		edit = false;
		toggle.setDisplayedChild(0);
		selected.clear();
		dia.notifyDataSetChanged();
	}

	public  void refresh()
	{
		loading.clear();
		loading.putAll(download.getAllTaskInfoWithState(false));
		loading.putAll(DownloadService.downloadlist);
		success.clear();
		success.putAll(download.getAllTaskInfoWithState(true));
		dia.notifyDataSetChanged();

	}
	@Subscribe(threadMode = ThreadMode.MainThread)
	public void updateTaskInfo(TaskInfo ti)
	{
		if (ti.isSuccess())
		{
			dia.notifyItemRemoved(loading.removeKey(ti.getId())+ 1);
			success.put(0,ti.getId(), ti);
			dia.notifyItemInserted(loading.size()+2);
			return;
		}
		if (ti.isDownloading()&&!isHidden())
		{
			
				if (loading.containsKey(ti.getId())&&!loading.containsValue(ti))
				{
					loading.put(ti.getId(), ti);
				}
				dia.notifyItemChanged(loading.indexKey(ti.getId()) + 1);
				//ti.setTime(System.currentTimeMillis());
		}
		else if(!ti.isDownloading())
		{
			dia.notifyItemChanged(loading.indexKey(ti.getId()) + 1);
		}
	}
	@Subscribe(threadMode=ThreadMode.MainThread)
	public void taskAdd(TaskBean tb)
	{
		switch (tb.getState())
		{
			case ADD:
				if (loading.containsKey(tb.getTaskInfo().getId()))
				{
					loading.put(tb.getTaskInfo().getId(), tb.getTaskInfo());
					dia.notifyItemChanged(loading.indexKey(tb.getTaskInfo().getId()) + 1);
				}
				else
				{
					loading.put(tb.getTaskInfo().getId(), tb.getTaskInfo());
					dia.notifyItemInserted(loading.size()+1);
				}
				break;
		}

	}
	@Override
	public boolean onBackPressed()
	{
		if (edit)
		{
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
