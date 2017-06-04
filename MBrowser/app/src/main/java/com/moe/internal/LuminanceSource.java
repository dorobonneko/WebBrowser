package com.moe.internal;
import android.graphics.Bitmap;

public class LuminanceSource extends com.google.zxing.LuminanceSource
{
	private byte[] data;
	public LuminanceSource(byte[] b,int width,int height){
		super(width,height);
		this.data=b;
	}
	public LuminanceSource(Bitmap bitmap){
		super(bitmap.getWidth(),bitmap.getHeight());
		final int[] tmp=new int[bitmap.getWidth()*bitmap.getHeight()];
		bitmap.getPixels(tmp, 0, getWidth(), 0, 0, getWidth(), getHeight());
		data=new byte[tmp.length];
		for(int i=0;i<data.length;i++){
			data[i]=(byte)tmp[i];
		}
	}
	@Override
	public byte[] getRow(int y, byte[] row)
	{
		System.arraycopy(data, y * getWidth(), row, 0, getWidth());
		return row;
	}

	@Override
	public byte[] getMatrix()
	{
		return data;
	}
	
}
