package com.moe.database;
import java.util.*;

public interface SearchHistory
{
	void insertSearchHistory(String data);
	List getSearchHistoryList();
	List querySearchHistory(String key);
	void clearSearchHistory();
}
