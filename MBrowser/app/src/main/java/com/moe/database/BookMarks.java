package com.moe.database;
import java.util.*;
import com.moe.entity.Bookmark;

public interface BookMarks
{
	List<Bookmark> loop(Bookmark b);
	void trimNo(Bookmark b);
	Bookmark getRoot();
	void update(Bookmark b);
	void insert(Bookmark b);
	void delete(Bookmark b);
	List<Bookmark> query(Bookmark num);
	Bookmark queryWithSon(int id);
	public class Type{
		public final static int FOLDER=0;
		public final static int BOOKMARK=1;
	}
}
