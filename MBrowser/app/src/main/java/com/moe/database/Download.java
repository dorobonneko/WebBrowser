package com.moe.database;
import com.moe.entity.TaskInfo;
import java.util.List;
import com.moe.download.DownloadTask;
import com.moe.entity.DownloadInfo;
import android.os.Environment;

public interface Download
{
	State addTaskInfo(TaskInfo ti);
	void deleteTaskInfoWithId(int id);
	void updateTaskInfo(TaskInfo ti);
	void updateTaskInfoData(TaskInfo ti);
	List<TaskInfo> getAllTaskInfo();
	List<TaskInfo> getAllTaskInfoWithState(boolean state);
	TaskInfo queryTaskInfoWithId(int id);
	void insertDownloadInfo(TaskInfo ti);
	void insertDownloadInfo(List<DownloadInfo> di);
	void insertDownloadInfo(DownloadInfo di);
	void deleteDownloadInfoWithId(int id);
	void updateDownloadInfo(TaskInfo ti);
	void updateDownloadInfo(List<DownloadInfo> di);
	void updateDownloadInfo(DownloadInfo di);
	List<DownloadInfo> getDownloadInfoWithId(int url);
	
	public enum State{
		UPDATE,SAMEURL,SAMENAME,SUCCESS;
	}
	public class Setting{
		//默认保存目录
		public final static String DIR="dir";
		public final static String DIR_DEFAULT=Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download";
		//默认下载任务数
		public final static String SIZE="size";
		public final static int SIZE_DEFAULE=3;
		//默认多线程数
		public final static String THREADSIZE="threadSize";
		public final static int THREADSIZE_DEFAULE=Runtime.getRuntime().availableProcessors();
		//默认多线程状态
		public final static String MULTITHREAD="multiThread";
		public final static boolean MULTITHREAD_DEFAULT=false;
		//缓冲
		public final static String BUFFER="buffer";
		public final static int BUFFER_DEFAULT=8092;
	}
}
