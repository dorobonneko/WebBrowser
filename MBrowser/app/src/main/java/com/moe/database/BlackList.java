package com.moe.database;

public interface BlackList
{
	int UNKNOW=0;
	int WHITE=1;
	int BLACK=2;
	void clear();
	void insertSite(String url,int state);
	int isBlackOrWhiteUrl(String url);
}
