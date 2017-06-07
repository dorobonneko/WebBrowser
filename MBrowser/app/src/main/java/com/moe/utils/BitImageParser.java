package com.moe.utils;
import android.graphics.Bitmap;
import java.net.URL;
import java.net.MalformedURLException;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.util.HashMap;
import com.google.zxing.DecodeHintType;
import java.util.Vector;
import com.google.zxing.BarcodeFormat;
import com.moe.internal.LuminanceSource;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.NotFoundException;
import com.google.zxing.qrcode.encoder.QRCode;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;

public class BitImageParser
{
	private static QRCodeReader qr;
	static{
		qr=new QRCodeReader();
		/**mfr=new MultiFormatReader();
		HashMap<DecodeHintType,Object> hm=new HashMap<>();
		Vector<BarcodeFormat> vector=new Vector<>();
		vector.add(BarcodeFormat.QR_CODE);
		vector.add(BarcodeFormat.DATA_MATRIX);
		vector.add(BarcodeFormat.AZTEC);
		vector.add(BarcodeFormat.MAXICODE);
		hm.put(DecodeHintType.POSSIBLE_FORMATS,vector);
		mfr.setHints(hm);*/
	}
	public static void decodeImage(byte[] data,Callback call,int width,int height){
		try
		{
			if (call != null)call.onSuccess(qr.decode(new BinaryBitmap(new HybridBinarizer(new LuminanceSource(data,width,height)))).getText());
		}
		catch (Exception e)
		{if(call!=null)call.onFail(e);}
		
	}
	public static void decodeImage(Bitmap bitmap,Callback call){
			try
			{
				if (call != null)call.onSuccess(qr.decode(new BinaryBitmap(new HybridBinarizer(new LuminanceSource(bitmap)))).getText());
			}
			catch (Exception e)
		{if(call!=null)call.onFail(e);}
			
			if(bitmap!=null) bitmap.recycle();
	}
	public static void decodeImageUrl(String url,Callback call){
		try
		{
			decodeImageUrl(new URL(url),call);
		}
		catch (MalformedURLException e)
		{if(call!=null)call.onFail(e);}
	}
	public static void decodeImageUrl(final URL url,final Callback call) {
		new Thread(){
			public void run(){
				try
				{
					decodeImage(BitmapFactory.decodeStream(url.openStream()),call);
				}
				catch (IOException e)
				{if(call!=null)call.onFail(e);}
			}
		}.start();
	}
	public abstract interface Callback{
		void onSuccess(String data);
		void onFail(Exception e);
	}
}
