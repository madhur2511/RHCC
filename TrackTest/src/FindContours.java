import org.opencv.imgproc.*;
import org.opencv.utils.Converters;
import org.opencv.core.*; 
import org.opencv.highgui.*; 

import java.util.*;

import javax.print.attribute.standard.Finishings;

class WatershedSegmenter{
    public Mat markers = new Mat();

    public void setMarkers(Mat markerImage)
    {
        markerImage.convertTo(markers, CvType.CV_32S);
    }

    public Mat process(Mat image)
    {
        Imgproc.watershed(image, markers);
        markers.convertTo(markers,CvType.CV_8U);
        return markers;
    }
}

public class FindContours
{

	public static void main(String args[])
	{
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
	    Mat image = Highgui.imread("C:/Users/Anand/workspace/OpenCV/saved8.png", Imgproc.COLOR_BGR2GRAY);
	    Mat imageHSV = new Mat(image.size(), Core.DEPTH_MASK_8U);
	    Mat imageBlurr = new Mat(image.size(), Core.DEPTH_MASK_8U);
	    Mat imageAB = new Mat(image.size(), Core.DEPTH_MASK_ALL);
	    Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
	    Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5,5), 0);
	    Imgproc.adaptiveThreshold(imageBlurr, imageAB, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV,7, 5);

	    Highgui.imwrite("C:/Users/Anand/workspace/OpenCV/test1.png",imageBlurr);
	    
	    double max=0.0;
	    int max_index=0;
	    double tempcontourarea=0.0;
	    Imgproc.Canny(imageAB, imageAB, 100, 300);
	    Highgui.imwrite("C:/Users/Anand/workspace/OpenCV/test2.png", imageAB);
	    
	    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();    
	    
	    Imgproc.findContours(imageAB, contours, new Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
	    
	    for(int i=0; i< contours.size();i++){
	        System.out.println(Imgproc.contourArea(contours.get(i)));
	        tempcontourarea=Imgproc.contourArea(contours.get(i));
	        if(tempcontourarea>max)
	        {
	        	max=tempcontourarea;
	        	max_index=i;
	        }
        }
	    	    
	    MatOfPoint2f conto = new MatOfPoint2f();
    	MatOfPoint2f approx = new MatOfPoint2f();            	
    	Point ptrrect[] =new Point[4];
    	
    	contours.get(max_index).convertTo(conto, CvType.CV_32FC2);
    	
    	RotatedRect rrect = Imgproc.minAreaRect(conto);    	    	
    	//Rect rect = Imgproc.boundingRect(contours.get(max_index));		
		
    	rrect.points(ptrrect);    	
    	for(Point p:ptrrect)
			Core.circle(image, p, 8, new Scalar(186,23,219),3);
    	
    	Size size = new Size();						
    	double angle = rrect.angle;
    	
    	if(rrect.size.height > rrect.size.width) {
    		rrect.size = new Size(rrect.size.height, rrect.size.width);
    		rrect.angle = 90 + rrect.angle;    
    	}
    	
    	Rect rect = rrect.boundingRect();
    	
    	if(rect.x < 0) 
    		rect.x = 0;
    	if(rect.y < 0) 
    		rect.y = 0;
    	
    	Point[] transPt = transform(rect,rrect);    	
    	sortCorners(transPt, rrect.center);    	    	    	
    	
    	Mat contour = Imgproc.getRotationMatrix2D(rrect.center, rrect.angle, 1.0);
    	Mat rotated = new Mat();
    	Mat cropped = new Mat();    	
    	
    	Imgproc.warpAffine(image, rotated, contour, size);    		
    	Imgproc.getRectSubPix(rotated, rrect.size, rrect.center, cropped);    	    	
	    	        	      
	    Highgui.imwrite("C:/Users/Anand/workspace/OpenCV/zz.png",rotated);	    
	    
	    Rect a = rect;
	    Mat retreive = new Mat(image.rows(),image.cols(),image.type());	 
	    Mat retreiverotate = new Mat(); 	    
	    cropped.copyTo(retreive.submat((int)transPt[0].y, (int)transPt[0].y + cropped.rows() , (int)transPt[0].x, (int)transPt[0].x + cropped.cols()));
	    
	    Mat contourInverse = Imgproc.getRotationMatrix2D(rrect.center,  - rrect.angle, 1.0);
	    Imgproc.warpAffine(retreive, retreiverotate, contourInverse, retreive.size());	    
	    
	    Mat final1 = new Mat();
	    Core.addWeighted(image,0.2, retreiverotate, 0.8, 10, final1);	
	    
	    Highgui.imwrite("C:/Users/Anand/workspace/OpenCV/zzrx.png",final1);
	    
	}
	
	public static Point[] transform(Rect rect , RotatedRect rrect)
	{
		Point pt[] = new Point[4];		
		rrect.points(pt);
		sortCorners(pt, rrect.center);
		Point midPoint = new Point((pt[0].x + pt[3].x)/2, (pt[0].y + pt[3].y)/2);		
		double h = distance(rrect.center, midPoint);
		double p = midPoint.y - rrect.center.y;				
		double rad = Math.asin(p/h);				

		for(int i = 0; i < 4; ++i) {
			pt[i] = rotateAngle(pt[i], rrect.center, rad);
		}
		return pt;
	}
	
	public static void sortCorners(Point []corners, Point center)
	{
		List<Point> top = new ArrayList<Point>();
		List<Point> bot = new ArrayList<Point>();	    

	    for (int i = 0; i < corners.length; i++)
	    {
	        if (corners[i].y < center.y)
	            top.add(corners[i]);
	        else
	            bot.add(corners[i]);
	    }

	    
	    Point tl = top.get(0).x > top.get(1).x ? top.get(1) : top.get(0);
	    Point tr = top.get(0).x > top.get(1).x ? top.get(0) : top.get(1);
	    Point bl = bot.get(0).x > bot.get(1).x ? bot.get(1) : bot.get(0);
	    Point br = bot.get(0).x > bot.get(1).x ? bot.get(0) : bot.get(1);	    
	    
	    corners[0] = tl;
	    corners[1] = tr;
	    corners[2] = br;
	    corners[3] = bl;	    	    
	}
	
	public static double distance(Point one, Point two)
	{
		double x1 = one.x;double y1 = one.y;
		double x2 = two.x;double y2 = two.x;
		
		return Math.sqrt(((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)));
	}
	
	public static Point rotateAngle(Point p, Point center, double theta) 
	{
		Point rp = new Point();
		rp.x = Math.cos(theta) * (p.x - center.x) - Math.sin(theta) * (p.y - center.y) + center.x;
		rp.y = Math.sin(theta) * (p.x - center.x) + Math.cos(theta) * (p.y - center.y)+ center.y;  		
		return rp;
	}	
}