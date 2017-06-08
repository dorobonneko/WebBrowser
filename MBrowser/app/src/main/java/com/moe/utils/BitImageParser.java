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
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import com.google.zxing.MultiFormatReader;

public class BitImageParser
{
	private static MultiFormatReader mfr;
	private static HashMap<DecodeHintType,Object> hm;
	static{
		mfr=new MultiFormatReader();
		hm=new HashMap<>();
		Vector<BarcodeFormat> vector=new Vector<>();
		vector.add(BarcodeFormat.QR_CODE);
		vector.add(BarcodeFormat.DATA_MATRIX);
		vector.add(BarcodeFormat.AZTEC);
		vector.add(BarcodeFormat.MAXICODE);
		vector.add(BarcodeFormat.CODE_128);
		vector.add(BarcodeFormat.CODE_39);
		vector.add(BarcodeFormat.CODE_93);
		vector.add(BarcodeFormat.CODABAR);
		vector.add(BarcodeFormat.EAN_13);
		vector.add(BarcodeFormat.EAN_8);
		vector.add(BarcodeFormat.ITF);
		vector.add(BarcodeFormat.PDF_417);
		vector.add(BarcodeFormat.RSS_14);
		vector.add(BarcodeFormat.RSS_EXPANDED);
		vector.add(BarcodeFormat.UPC_A);
		vector.add(BarcodeFormat.UPC_E);
		vector.add(BarcodeFormat.UPC_EAN_EXTENSION);
		hm.put(DecodeHintType.POSSIBLE_FORMATS,vector);
		hm.put(DecodeHintType.CHARACTER_SET, "UTF8");
		mfr.setHints(hm);
	}
	public static void decodeImage(byte[] data,Callback call,int width,int height){
		try
		{
			if (call != null)call.onSuccess(mfr.decode(new BinaryBitmap(new HybridBinarizer(new LuminanceSource(data,width,height)))).getText());
		}
		catch (Exception e)
		{if(call!=null)call.onFail(e);}
		
	}
	public static void decodeImage(Bitmap bitmap,Callback call){
			try
			{
				if (call != null)call.onSuccess(mfr.decode(new BinaryBitmap(new HybridBinarizer(new LuminanceSource(bitmap)))).getText());
			}
			catch (Exception e)
		{if(call!=null)call.onFail(e);}
			
			if(bitmap!=null) bitmap.recycle();
	}
	public static void decodeImage(InputStream is,Callback call){
		decodeImage(BitmapFactory.decodeStream(is),call);
		try
		{
			is.close();
		}
		catch (IOException e)
		{}
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
					decodeImage(url.openStream(),call);
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
