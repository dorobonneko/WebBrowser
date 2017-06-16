package com.moe.net;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSession;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLSocketFactory;

public class OkHttp
{
	private static OkHttpClient okhttp;
	private static SSLSocketFactory ssf;
	public static OkHttpClient getOkHttp(){
		if(okhttp==null){
			
			okhttp = new OkHttpClient.Builder().sslSocketFactory(getSslSocketFactory()).hostnameVerifier(new HostnameVerifier(){

					@Override
					public boolean verify(String p1, SSLSession p2)
					{
						return true;
					}
				}).connectTimeout(30,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).build();

		}
		return okhttp;
	}
	public static SSLSocketFactory getSslSocketFactory(){
		if(ssf==null){
			SSLContext ssl=null;
			try
			{
				ssl = SSLContext.getInstance("TLS");
				ssl.init(null, new TrustManager[]{new X509TrustManager()}, new SecureRandom());
			}
			catch (NoSuchAlgorithmException e)
			{}
			catch (KeyManagementException e)
			{}
			ssf=ssl.getSocketFactory();
		}
		return ssf;
	}
}
