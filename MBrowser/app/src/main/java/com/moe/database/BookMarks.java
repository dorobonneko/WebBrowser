package com.moe.database;
import java.util.*;
import com.moe.entity.Bookmark;
import java.io.File;

public interface BookMarks
{

	void importData(File file);
	List<Bookmark> loop(Bookmark b);
	void trimNo(Bookmark b);
	Bookmark getRoot();
	void update(Bookmark old,Bookmark b);
	void insert(Bookmark b);
	void delete(Bookmark b);
	List<Bookmark> query(Bookmark num);
	Bookmark queryWithPath(String path);
	public class Type{
		public final static int FOLDER=0;
		public final static int BOOKMARK=1;
	}
}
