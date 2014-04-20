import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Main2 {
	
	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	public static String ip= "ws://192.168.1.3";
	private JFrame window;	
	private ImagePanel ip1;
	private ImagePanel ip3;		
	private VideoCapture video = null;	
	private WebClient webclient;
	private Boolean begin = false;
	private Mat frameInternal = new Mat();
	private String defaultloc;	
	private String sendStringShared = "";
	private String receiveStringShared = "";	
	private boolean guiUpdate = true;
	private boolean websocketUpdate = true;
	private final static int WIDTH = 640;
	private final static int HEIGHT = 480;
	private RotatedRect backupRrect;
	
	public Main2(String defaultloc) throws URISyntaxException
	{		
		buildGUI();
		this.defaultloc = defaultloc;	
		this.backupRrect = new RotatedRect(new Point(320, 240), new Size(WIDTH, HEIGHT), 0);
	}		
	
	public void showPopup() {
		//STANDARD JDialog code      
		
        final JTextArea username_TA = new JTextArea();
        final JTextArea groupname_TA = new JTextArea();         
        final JComponent[] inputs = new JComponent[] {  
                  new JLabel("Username:"),  
                  username_TA,  
                  new JLabel("Group:"),  
                  groupname_TA  
        };  
        int result = JOptionPane.showConfirmDialog(window, inputs, "Collaborator", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);          
        if(result == JOptionPane.OK_OPTION){        	
        	try {
        		String usernamename = username_TA.getText().trim();        	
            	String groupname = groupname_TA.getText().trim();
	        	
	        	if(usernamename.length()!=0 && groupname.length()!=0){
	        		String params = "?name="+usernamename+"&group="+groupname;
	        		start(ip.concat(params));
	        	} else {
	        		String params = "?name="+"m"+"&group="+"k";
	        		start(ip.concat(params));
	        	}
	        	
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}            
        } else {  
        	window.dispose();
        }  
        window.requestFocus();
	}
	
	public void buildGUI()
	{
	    window = new JFrame("Realtime Content Collaboration");
	    
	    Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screen = tk.getScreenSize();
		int width = screen.width;
		int height = screen.height;
		
		System.out.println(width + " x " + height);	    	    	    
		
		ip3 = new ImagePanel();
		ip3.setBounds(0 , 0, width, height);
		ip3.setBorder(BorderFactory.createLineBorder(Color.black));
		window.add(ip3);
		
//		ip1 = new ImagePanel();
//		ip1.setBounds(0,0, 640, 480);
//		ip1.setBorder(BorderFactory.createLineBorder(Color.black));
//		window.add(ip1);
	   		
		window.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {								
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
					
				// optimize points
				if(arg0.getKeyCode() == 79) {
					// press shift x
					optimizePoints();
				}
				
				if(arg0.getKeyCode() == 88) {
					// press shift x
					stop();
				}
				
				if(arg0.getKeyCode() == 83) {
					save();
				}
				
				if(arg0.getKeyCode() == 27) {
					stop();
					window.dispose();
					System.exit(0);
				}					
			}

			@Override
			public void keyTyped(KeyEvent arg0) {				
			}
			
		});
        
        window.setUndecorated(true);
	    window.setSize(width,height);
		window.setVisible(true);
	    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	    	   
	    
	    showPopup();
	}
	
	private void optimizePoints() {
		if(frameInternal != null) {
			
			Mat originalFrame = frameInternal;					
			Mat returnMat = null;
			Mat imageHSV = new Mat(originalFrame.size(), Core.DEPTH_MASK_8U);
		    Mat imageBlurr = new Mat(originalFrame.size(), Core.DEPTH_MASK_8U);
		    Mat imageAB = new Mat(originalFrame.size(), Core.DEPTH_MASK_ALL);
		    Imgproc.cvtColor(originalFrame, imageHSV, Imgproc.COLOR_BGR2GRAY);
		    Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5,5), 0);
		    Imgproc.adaptiveThreshold(imageBlurr, imageAB, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV,7, 5);
		    
		    double max=0.0;
		    int max_index=0;
		    double tempcontourarea=0.0;
		    Imgproc.Canny(imageAB, imageAB, 100, 300);
		    
		    
		    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();    
		    
		    Imgproc.findContours(imageAB, contours, new Mat(), Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_NONE);
		    
		    for(int i=0; i< contours.size();i++){
		        tempcontourarea=Imgproc.contourArea(contours.get(i));
		        if(tempcontourarea>max)
		        {
		        	max=tempcontourarea;
		        	max_index=i;
		        }
	        }
		    
		    if(contours.size() != 0 && max > 40000) {
		    
			    MatOfPoint2f conto = new MatOfPoint2f();
		    	MatOfPoint2f approx = new MatOfPoint2f();            	
		    	Point ptrrect[] =new Point[4];
		    	
		    	contours.get(max_index).convertTo(conto, CvType.CV_32FC2);
		    	
		    	RotatedRect rrect = Imgproc.minAreaRect(conto);  
	//	    	rrect.points(ptrrect);    	
	//	    	for(Point p:ptrrect)
	//				Core.circle(originalFrame, p, 8, new Scalar(186,23,219),3);
	//	    		    	 	    						     	
		     	
		     	if(rrect.size.height > rrect.size.width) {
		     		rrect.size = new Size(rrect.size.height, rrect.size.width);
		     		rrect.angle = 90 + rrect.angle;    
		     	}	     	
		    	setBackupRotatedRect(rrect);
		    }
		    else {
		    	setBackupRotatedRect(new RotatedRect(new Point(320, 240), new Size(WIDTH, HEIGHT), 0));
		    }
		    	 
		}
	}
	
	private void startGUIUpdate()
	{
		new Thread(new Runnable(){
			
			@Override
			public void run()
			{
				Mat frameResizedInternal = new Mat(HEIGHT,WIDTH,CvType.CV_8UC3);
				byte[] byteArrayInternal;				        
		        MatOfByte matOfByteInternal = new MatOfByte(); 
				while(guiUpdate == true)
				{
					// send webcam feed
					video.read(frameInternal);
			        video.retrieve(frameInternal);
			        Point pt[] = new Point[4];			    
			        backupRrect.points(pt);
			        for(Point p:pt)
						Core.circle(frameInternal, p, 8, new Scalar(186,23,219),3);
			        if(frameInternal != null) {			        	
			        	Mat modifiedFrameInternal = imageTransform(frameInternal);			        	
			        	if(modifiedFrameInternal == null){
			        		modifiedFrameInternal = frameInternal;
			        	}			  			        				        	
				        Imgproc.resize(modifiedFrameInternal, frameResizedInternal, frameResizedInternal.size());				        
				        Highgui.imencode(".png", frameResizedInternal, matOfByteInternal);				        
					    byteArrayInternal = matOfByteInternal.toArray();
					    String base64  = Base64.encode(byteArrayInternal);
					    setSendStringShared(base64);							    
			        }				    
				    // update Screen			        
					BufferedImage bufImageCombined = ImageUtils.stringToImage(receiveStringShared);
			        if(bufImageCombined != null) {
			        	ip3.updateImage(bufImageCombined);
			        }			       			        
				}				
			}			
			
		}).start();
	}
	
	private BufferedImage BufferedImagefromMat(Mat image) {
		BufferedImage buff = null;
		MatOfByte mob = new MatOfByte();
		Highgui.imencode(".png", image, mob);				        
	    byte[] bytearray = mob.toArray();
	    String base64 = Base64.encode(bytearray);
	    buff = ImageUtils.stringToImage(base64);
		return buff;
	}
	
	private void startWebSocketUpdate()
	{
		new Thread(new Runnable(){
				
			@Override
			public void run()
			{			
				while(websocketUpdate == true) 
				{					
					// receive string update			
					Mat frameExternal = null;					
			     	BufferedImage croppedRecBufferedImage = ImageUtils.stringToImage(webclient.getMessage());			     	
			     	if(!webclient.getMessage().equals("/") && croppedRecBufferedImage != null) {				     		
			     		synchronized (backupRrect) {															     			
					 	    Rect rect = backupRrect.boundingRect();				     	
					     	if(rect.x < 0) 
					     		rect.x = 0;
					     	if(rect.y < 0) 
					     		rect.y = 0;	
					     	Point[] transPt = transform(rect,backupRrect);    	
					     	sortCorners(transPt, backupRrect.center);
					     	
							byte[] data = ((DataBufferByte) croppedRecBufferedImage.getRaster().getDataBuffer()).getData();
							frameExternal = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
							frameExternal.put(0, 0, data);										
							Mat resizedFrameExternal = new Mat((int)backupRrect.size.height, (int)backupRrect.size.width , CvType.CV_8UC3);						
					     	Imgproc.resize(frameExternal, resizedFrameExternal, new Size((int)backupRrect.size.width, (int)backupRrect.size.height));					     	
					 	    Mat retreive = new Mat(HEIGHT ,WIDTH , frameInternal.type());	 
					 	    Mat retreiverotate = new Mat(); 	    					 	    			     					     	 
					 	    resizedFrameExternal.copyTo(retreive.submat((int)transPt[0].y, (int)transPt[0].y + resizedFrameExternal.rows() , (int)transPt[0].x, (int)transPt[0].x + resizedFrameExternal.cols()));					 	    					 	    
					 	    Mat contourInverseTransformation = Imgproc.getRotationMatrix2D(backupRrect.center,  - backupRrect.angle, 1.0);
					 	    Imgproc.warpAffine(retreive, retreiverotate, contourInverseTransformation, retreive.size());	    				 	    
					 	    //Core.addWeighted(modified , 0.2, retreiverotate, 0.8, 10, modified);	
					 	    MatOfByte matOfByteCombined= new MatOfByte();						        						       			        			           
						    Highgui.imencode(".png", retreiverotate, matOfByteCombined);
						    byte[] byteArrayCombined = matOfByteCombined.toArray();
						    String base64 = Base64.encode(byteArrayCombined);
						    setReceiveStringShared(base64);		 
			     		}
			     	}					
					if(sendStringShared != "") {
						webclient.send(sendStringShared);						
						try {
							Thread.sleep(40);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else 
					{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			
		}).start();
	}
	
	private void start(String loc) throws URISyntaxException
	{
		Draft[] drafts = { new Draft_17(), new Draft_10(), new Draft_76(), new Draft_75() };
		System.out.println("Connecting to Server: "+loc);			
		webclient = new WebClient(new URI(loc), drafts[0]);
		if(begin == false)
		{
			webclient.connect();
			video = new VideoCapture(0);			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			startGUIUpdate();
			startWebSocketUpdate();
			begin = true;
		}
	}
	
	
	private void save()
	{
		BufferedImage savimg = (BufferedImage)ip3.getImage();
		if(savimg != null)
		{
			byte[] data = ((DataBufferByte) savimg.getRaster().getDataBuffer()).getData();
			Mat saveframe = new Mat(HEIGHT,WIDTH, CvType.CV_8UC3);
			saveframe.put(0, 0, data);					        				        											        						        
			Highgui.imwrite("saved11.png",saveframe);
			System.out.println("Image Saved ...");
		}else
		{
			System.out.println("Cannot save Image.. Non existant remote Connection!");
		}
	}
	
	
	private void stop()
	{		
		if(begin == true) {
			guiUpdate = false;
			websocketUpdate = false;
			webclient.close();
			video.release();			
		}		
	}
	
	
	public Mat imageTransform(Mat original)
	{				     	
		RotatedRect rrect = backupRrect; 	  
	     
     	Rect rect = rrect.boundingRect();
     	Size size = new Size();
     	if(rect.x < 0) 
     		rect.x = 0;
     	if(rect.y < 0) 
     		rect.y = 0;
     	
     	Point[] transPt = transform(rect,rrect);    	
     	sortCorners(transPt, rrect.center);    	    	    	
     	
     	Mat transformMatrix = Imgproc.getRotationMatrix2D(rrect.center, rrect.angle, 1.0);
     	Mat rotated = new Mat();
     	Mat cropped = new Mat();    	
     	
     	Imgproc.warpAffine(original, rotated, transformMatrix, size);    		
     	Imgproc.getRectSubPix(rotated, rrect.size, rrect.center, cropped);    	    		        	      
 	    
     	return cropped;     	     	     	     		     		 	    	    	    	   
	}
	
	
	private void setSendStringShared(String string) 
	{
		synchronized(sendStringShared) 
		{
			sendStringShared = string;
		}
	}
	
	private void setReceiveStringShared(String string) 
	{
		synchronized(receiveStringShared) 
		{
			receiveStringShared = string;
		}
	}	
	
	public static void main(String args[]) throws NumberFormatException, IOException, URISyntaxException
	{
		WebSocketImpl.DEBUG = false;
		String loc;
		int port=1018;
		BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Anand/workspace/port.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			port = Integer.parseInt(line);
		}
		
		if( args.length != 0 ) {
			loc = args[ 0 ];
			System.out.println( "Default server url specified: \'" + loc + "\'" );
		} else {
			
			Main2.ip = (Main2.ip).concat(":"+port);			
			System.out.println( "Default server url not specified: defaulting to \'" + Main2.ip + "\'" );
		}
						
		
		Main2 m3 = new Main2(Main2.ip);
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
	
	public void setBackupRotatedRect(RotatedRect rrect) {
		synchronized (backupRrect) {
			backupRrect = rrect;	
		}		
	}
	
			
}

class WebClient extends WebSocketClient
{
	private String message = "";
	
	public String getMessage()
	{
		return message;
	}
	
	public WebClient(URI uri, Draft draft)
	{
		super(uri, draft);
	}
	
	@Override
	public void onMessage( String message ) {
		this.message = message;		
	}

	@Override
	public void onOpen( ServerHandshake handshake ) {
		System.out.println( "You are connected to RHCCServer: " + getURI() + "\n" );						
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		System.out.println("Closed because "+reason+code);
		this.close();
	}

	@Override
	public void onError( Exception ex ) {
		System.out.println(ex.toString());
	}	
}
