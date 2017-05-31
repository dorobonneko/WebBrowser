package com.moe.database;
import java.util.List;

public interface JavaScript
{

	 List<String> getAllScript(String host);
	void addScript(String name,String content);
	void updateScript(int id,String name,String content);
	void deleteScript(int id);
	void updateScriptState(int id,boolean state);
	List<Object[]> getAllScript();
}
