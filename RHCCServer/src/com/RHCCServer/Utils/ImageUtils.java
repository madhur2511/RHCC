package com.RHCCServer.Utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.java_websocket.util.Base64;


public class ImageUtils {

    public static BufferedImage stringToImage(String string) {

        BufferedImage image = null;
        byte[] bytes;
        if(string == null){}
        else{
		    try {
		    	bytes = Base64.decode(string);
		        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		        image = ImageIO.read(bis);
		        bis.close();
		        
		    } catch (Exception e) {
		       // e.printStackTrace();
		    	image = null;
		    }
        }
        return image;
    }

   
    public static String imageToString(BufferedImage image, String type) {
        String string = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] bytes = bos.toByteArray();
            string = Base64.encodeBytes(bytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }
}
