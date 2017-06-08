package com.moe.utils;
import com.google.zxing.common.BitMatrix;
import android.graphics.Bitmap;

public class BitMatrixToBitmap
{
	public static Bitmap convert(BitMatrix bm){
		int w = bm.getWidth();  
        int h = bm.getHeight();  
        int[] data = new int[w * h];  

        for (int y = 0; y < h; y++) {  
            for (int x = 0; x < w; x++) {  
                if (bm.get(x, y))  
                    data[y * w + x] = 0xff000000;// 黑色  
                else  
                    data[y * w + x] = -1;// -1 相当于0xffffffff 白色  
            }  
        }  
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);  
        bitmap.setPixels(data, 0, w, 0, 0, w, h);  
		return bitmap;
	}
}
