package com.moe.utils;
import android.content.Context;
import android.net.Uri;
import android.content.Intent;

public class UrlParse
{
    public static boolean parser(String url,android.webkit.WebView wv,Context context){
        Intent intent=null;
        // 调用拨号程序
        if (url.startsWith("tel:")) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            
        }
        // 发送短信
        else if (url.startsWith("sms:")) {
            Uri uri = Uri.parse(url);
            intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra("sms_body", "");
           
        }
        // 导航
        else if (url.startsWith("geo:")) {
            Uri uri = Uri.parse("geo:0,0?q=30.732993,120.756175,地址");
            intent = new Intent(Intent.ACTION_VIEW, uri);
           
        }
        // 返回
        else if (url.startsWith("go:")) {
            if (url.startsWith("go:back")) {
                if (wv.canGoBack()) {
                    wv.stopLoading();
                    wv.goBack();
                } else {//go:finish
                    //finish();
                }
            }
            return true;
            }
            else if(url.startsWith("http:")||url.startsWith("https")){
                wv.loadUrl(url);
                return true;
            }
        if(intent!=null){
            context.startActivity(intent);
            return true;
        }
        return false;
    }
}
