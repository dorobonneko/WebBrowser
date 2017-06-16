package com.moe.net;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;

public class X509TrustManager implements X509TrustManager{

	@Override
	public void checkClientTrusted(X509Certificate[] p1, String p2) throws CertificateException
	{
	}

	@Override
	public void checkServerTrusted(X509Certificate[] p1, String p2) throws CertificateException
	{}

	@Override
	public X509Certificate[] getAcceptedIssuers()
	{
		return new X509Certificate[0];
	}
}
