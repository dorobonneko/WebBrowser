package com.moe.bean;

public class SubSiteBean
{
    private String name,url,icon;

public SubSiteBean(String name,String url){
    this.name=name;
    this.url=url;
}
    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getUrl()
    {
        return url;
    }

    public void setIcon(String icon)
    {
        this.icon = icon;
    }

    public String getIcon()
    {
        return icon;
    }}
