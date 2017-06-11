package com.moe.database;
import com.moe.entity.TaskInfo;
import java.util.List;
import com.moe.download.DownloadTask;
import com.moe.entity.DownloadInfo;
import android.os.Environment;
import com.moe.database.Download.State;
import java.util.Map;

public interface Download
{

	void renameTask(int id, String toString);

	void clearAllTask(TaskInfo[] id,boolean file);
	void clearAllSuccessTask(boolean file);

	void addTaskInfo(TaskInfo ti,Callback call);
	void deleteTaskInfoWithId(int id);
	void updateTaskInfo(TaskInfo ti);
	void updateTaskInfoData(TaskInfo ti);
	Map<Integer,TaskInfo> getAllTaskInfoWithState(boolean state);
	TaskInfo queryTaskInfoWithId(int id);
	void insertDownloadInfo(TaskInfo ti);
	void insertDownloadInfo(List<DownloadInfo> di);
	void insertDownloadInfo(DownloadInfo di);
	void deleteDownloadInfoWithId(int id);
	void updateDownloadInfo(TaskInfo ti);
	void updateDownloadInfo(List<DownloadInfo> di);
	void updateDownloadInfo(DownloadInfo di);
	List<DownloadInfo> getDownloadInfoWithId(int url);
	void updateDownloadInfoWithData(DownloadInfo di);
	
	public enum State{
		UPDATE,SUCCESS,FAIL;
	}
	public class Setting{
		//默认保存目录
		public final static String DIR="dir";
		public final static String DIR_DEFAULT=Environment.getExternalStorageDirectory().getAbsolutePath()+"/MoeBrowser";
		//默认下载任务数
		public final static String SIZE="size";
		public final static int SIZE_DEFAULT=1;
		//默认多线程数
		public final static String THREADSIZE="threadSize";
		public final static int THREADSIZE_DEFAULE=Runtime.getRuntime().availableProcessors();
		//默认多线程状态
		public final static String MULTITHREAD="multiThread";
		public final static boolean MULTITHREAD_DEFAULT=false;
		//缓冲
		public final static String BUFFER="buffer";
		public final static int BUFFER_DEFAULT=2;
		//重试次数
		public final static String RELOADSIZE="reloadSize";
		public final static int RELOADSIZE_DEFAULT=2;
		//M3U8出错
		public final static String M3U8ERROR="m3u8Error";
		public final static int M3U8ERROR_DEFAULT=0;
		
	}
	public abstract interface Callback{
		void callback(TaskInfo ti,State state);
	}
}
