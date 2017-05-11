package com.moe.bean;
import com.moe.entity.TaskInfo;

public class TaskBean
{
	private TaskInfo taskInfo;
	private State state;

	public TaskBean(TaskInfo taskInfo, State state)
	{
		this.taskInfo = taskInfo;
		this.state = state;
	}

	public void setState(State state)
	{
		this.state = state;
	}

	public State getState()
	{
		return state;
	}

	public void setTaskInfo(TaskInfo taskInfo)
	{
		this.taskInfo = taskInfo;
	}

	public TaskInfo getTaskInfo()
	{
		return taskInfo;
	}

	public enum State{
		ADD,UPDATE,DELETE;
	}
}
