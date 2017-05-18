package com.moe.database;
import java.util.*;

public interface BookMarks
{

	public void updataBookmark(String toString, String p1, String p2, String currenturl);


	public String[] getBookmark(String url);


	void changeFolder(String str, String dir);
	void insertBookmark(String url, String title, String dir);
	void createFolder(String name);
	void deleteFolder(String name);
	void insertBookmark(String url,String title);
	void moveToDirectory(String url,String dir);
	void deleteBookmark(String url);
	void moveToIndex(String url,int index);
	void moveGroupToIndex(String url,int index);
	List getAllBookmarkGroup();
	List queryBookmark(String dir);
	boolean isBookmark(String url);
}
