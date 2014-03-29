package com.rhccclientmobile.utils;

import java.io.ByteArrayOutputStream;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class ImageUtils {
	
	public static Bitmap decodeImage(String message) {
		Bitmap decodedByte = null;
		Bitmap resized = null;
		
		try {
			byte[] decodedString = Base64.decode(message, Base64.DEFAULT);
			decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);	
			resized = Bitmap.createScaledBitmap(decodedByte, 320, 240, true);
		}
		catch(Exception e) {
			
		}
		return resized;
	}
	
	public static String encodeImage(Bitmap image) {	
		String imageEncoded = null;
		try {
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		    Bitmap resized = Bitmap.createScaledBitmap(image, 320, 240, false);
		    resized.compress(Bitmap.CompressFormat.JPEG , 100, baos);
		    byte[] b = baos.toByteArray();
		    imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
		}
		catch(Exception e) {
			
		}
	    return imageEncoded;
	}
}