import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


// A Hough Lines method of detecting contours(boundary of paper) by detecting straight lines
public class HoughLines {

	public static void main(String[] args)
	{
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		Mat image1 = Highgui.imread("C:/Users/Anand/workspace/OpenCV/saved1.png");
		
	    Mat imageHSV1 = new Mat(image1.size(), Core.DEPTH_MASK_8U);
	    Mat imageBlurr1 = new Mat(image1.size(), Core.DEPTH_MASK_8U);
	    Mat imageA = new Mat(image1.size(), Core.DEPTH_MASK_8U);
	    Mat lines = new Mat();
	    Mat lineDisp = new Mat();
		    
		Imgproc.cvtColor(image1, imageHSV1, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(imageHSV1, imageBlurr1, new Size(3,3), 0.0);
		
		Imgproc.Canny(imageBlurr1, imageA, 100, 300);
		
	    Highgui.imwrite("C:/Users/Anand/workspace/OpenCV/track1.png",imageA);
		
	    lineDisp = imageA;
	    
		Imgproc.HoughLinesP(imageA, lines, 1, 3*Math.PI/180, 50, 20, 40);
	
		for (int x = 0; x < lines.cols(); x++) 
	    {
	          double[] vec = lines.get(0, x);
	          double x1 = vec[0], 
	                 y1 = vec[1],
	                 x2 = vec[2],
	                 y2 = vec[3];
	          Point start = new Point(x1, y1);
	          Point end = new Point(x2, y2);
	
	          Core.line(lineDisp, start, end, new Scalar(255,0,0), 5);
	    }
		
	    Highgui.imwrite("C:/Users/Anand/workspace/OpenCV/tracK2.0.png",lineDisp);
		
		ArrayList<Point> corners = new ArrayList<Point>();
		double tempX=0.0 , tempY=0.0;
		double avgX = 0.0 , avgY = 0.0;
		
		for (int i = 0; i < lines.cols(); i++)
		{
		    for (int j = i+1; j < lines.cols()-1; j++)
		    {
		    	Point pt = computeIntersect(lines.get(0,i), lines.get(0,j));
		        if (pt.x >= 0.0 && pt.y >= 0.0)
		        {
		        	if(Math.abs(tempX - pt.x) > 20 || Math.abs(tempY - pt.y) > 20 )
		        	{
		        		tempX = pt.x;
		        		tempY = pt.y;
		        		corners.add(pt);
		        	}
		        }
		    }
		}
		
		MatOfPoint2f cornersmop2f0 = new MatOfPoint2f();
		cornersmop2f0.fromList(corners);
		System.out.println(cornersmop2f0.rows());
		System.out.println(cornersmop2f0.dump());
		
		double d = 0.0;
		int i = 1;
		int n = corners.size();
		Point pfirst = corners.get(0);
		
		while(i < n)
		{
		    	d = distance(corners.get(i),pfirst);
		    	if(d < 50000)
		    	{
		    		corners.remove(i);
		    		n -= 1;
		    	}
		    	i += 1;
		}
		
		System.out.println(corners.size());
	
		for(Point p:corners)
			Core.circle(lineDisp, p, 8, new Scalar(186,23,219),3);
		
		Highgui.imwrite("C:/Users/Anand/workspace/OpenCV/track2.1.png",imageA);
			
		MatOfPoint2f approx = new MatOfPoint2f();
		MatOfPoint2f cornersmop2f = new MatOfPoint2f();
		cornersmop2f.fromList(corners);
		
		Imgproc.approxPolyDP(cornersmop2f, approx, Imgproc.arcLength(cornersmop2f, true) * 0.02, true);
		
		
		if (approx.cols() != 4)
		{
		    System.out.println("The object is not quadrilateral!");
		}
		
		
		System.out.println(cornersmop2f.dump());
	    
    }
				

	public static Point computeIntersect(double[] a, double[] b)
	{
	    int x1 = (int)a[0], y1 = (int)a[1], x2 = (int)a[2], y2 = (int)a[3];
	    int x3 = (int)b[0], y3 = (int)b[1], x4 = (int)b[2], y4 = (int)b[3];
	
	    double d = ((double)(x1-x2) * (y3-y4)) - ((y1-y2) * (x3-x4));
	    if (d > 200)
	    {
	        Point pt = new Point();
	        pt.x = ((x1*y2 - y1*x2) * (x3-x4) - (x1-x2) * (x3*y4 - y3*x4)) / d;
	        pt.y = ((x1*y2 - y1*x2) * (y3-y4) - (y1-y2) * (x3*y4 - y3*x4)) / d;
	        return pt;
	    }
	    else
	        return new Point(-1.0, -1.0);
	    
	}
	
	public static double distance(Point one, Point two)
	{
		double x1 = one.x;double y1 = one.y;
		double x2 = two.x;double y2 = two.x;
		
		return ((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
}
}
