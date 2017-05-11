package com.moe.download;

public class DownloadTask
{
	private State state=State.UNKNOW;
	public enum State{
		QUERY,DOWNLOADING,PAUSE,STOP,FAIL,INVALIDE,DISKLESS,UNKNOW,TEMPFILE,SUCCESS,NOPERMISSION;
		//查询信息，下载中，暂停，停止，失败，地址过期，磁盘空间不足，未知，创建临时文件，完成，无权限;
	}
}
