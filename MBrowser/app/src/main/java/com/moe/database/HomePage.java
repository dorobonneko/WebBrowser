package com.moe.database;
import java.util.List;

public interface HomePage
{
	String getJsonData();
	List<String[]> getData();
	void insertItem(String url,String title);
	void deleteItem(String url);
	//void moveItemToIndex(String url,int index);
}
