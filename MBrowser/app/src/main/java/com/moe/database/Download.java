package com.moe.database;
import com.moe.entity.TaskInfo;
import java.util.List;
import com.moe.download.DownloadTask;
import com.moe.entity.DownloadInfo;

public interface Download
{
	State addTaskInfo(TaskInfo ti);
	void deleteTaskInfoWithUrl(String url);
	void updateTaskInfo(TaskInfo ti);
	List<TaskInfo> getAllTaskInfo();
	List<TaskInfo> getAllTaskInfoWithState(boolean state);
	TaskInfo queryTaskInfoWithUrl(String url);
	void insertDownloadInfo(TaskInfo ti);
	void insertDownloadInfo(List<DownloadInfo> di);
	void deleteDownloadInfoWithUrl(String url);
	void updateDownloadInfo(TaskInfo ti);
	void updateDownloadInfo(List<DownloadInfo> di);
	void updateDownloadInfo(DownloadInfo di);
	List<DownloadInfo> getDownloadInfoWithUrl(String url);
	
	public enum State{
		UPDATE,SAMEURL,SAMENAME,SUCCESS;
	}
}
