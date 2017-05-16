package com.moe.database;
import java.util.*;

public interface WebHistory
{
	void insertOrUpdateWebHistory(String url,String title);
	List getWebHistory();
	void clearWebHistory();
	List queryWebHistory(String key);
	
}
