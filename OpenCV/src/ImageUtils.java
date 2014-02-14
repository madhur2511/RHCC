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
        try {
        	bytes = Base64.decode(string);
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            image = ImageIO.read(bis);
            bis.close();
            
        } catch (Exception e) {
            e.printStackTrace();
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

//    public static void main (String args[]) throws IOException {
//        
//        BufferedImage img = ImageIO.read(new File("C:/Users/Madhur/workspace/OpenCV/fig/opencv.png"));
//        BufferedImage newImg;
//        String imgstr;
//        imgstr = imageToString(img, "png");
//        System.out.println(imgstr);
//        newImg = stringToImage(imgstr);
//        ImageIO.write(newImg, "png", new File("C:/Users/Madhur/workspace/OpenCV/fig/opencv(temp).png"));
//        
//    }
}