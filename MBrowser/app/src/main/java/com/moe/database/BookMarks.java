package com.moe.database;
import java.util.*;

public interface BookMarks
{

	void insertBookmark(String url, String title, String dir);
	void createFolder(String name);
	void deleteFolder(String name);
	void insertBookmark(String url,String title);
	void moveToDirectory(String... url,String dir);
	void deleteBookmark(String... url);
	void moveToIndex(String url,int index);
	void moveGroupToIndex(String url,int index);
	List getAllBookmarkGroup();
	List queryBookmark(String dir);
	boolean isBookmark(String url);
}
