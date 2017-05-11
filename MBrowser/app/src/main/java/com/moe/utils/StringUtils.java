package com.moe.utils;
import java.io.InputStream;
import java.io.IOException;

public class StringUtils
{


    public static String newString(InputStream is) throws IOException
    {
        StringBuffer sb=new StringBuffer();
        int len=0;
        byte[] b=new byte[8092];
        while((len=is.read(b))!=-1){
            sb.append(new String(b,0,len));
        }
        is.close();
        return sb.toString();
    }}
