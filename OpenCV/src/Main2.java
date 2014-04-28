import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
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
	public static String ip = "ws://";
	private JFrame window;	
	private ImagePanel ip3;
	private VideoCapture video = null;	
	private WebClient webclient;
	private Boolean begin = false;
	private Mat frameInternal = new Mat();
	private String sendStringShared = "";
	private String receiveStringShared = "";	
	private boolean guiUpdate = true;
	private boolean websocketUpdate = true;
	private final static int WIDTH = 640;
	private final static int HEIGHT = 480;
	private RotatedRect backupRrect;
	private BufferedImage blackScreen;
	private BufferedImage whiteScreen;
	private BufferedImage messageScreen;
	
	
	// Builds the GUI and loads the required media
	public Main2() throws URISyntaxException
	{				
		this.backupRrect = new RotatedRect(new Point(WIDTH/2, HEIGHT/2), new Size(WIDTH, HEIGHT), 0);
		File fileBlackScreen = new File("C:/Users/Anand/workspace/OpenCV/Black.png");
		File fileWhiteScreen = new File("C:/Users/Anand/workspace/OpenCV/White.png");
		File fileMessageScreen = new File("C:/Users/Anand/workspace/OpenCV/Message.png");
		
		try {
			blackScreen = ImageIO.read(fileBlackScreen);
			whiteScreen = ImageIO.read(fileWhiteScreen);
			messageScreen = ImageIO.read(fileMessageScreen);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		buildGUI();		
	}		
	
	
	// Presents the Dialog Box to the user on start up
	public void showPopup() {
		
		final JTextArea ip_TA = new JTextArea();
        final JTextArea username_TA = new JTextArea();
        final JTextArea groupname_TA = new JTextArea();         
        final JComponent[] inputs = new JComponent[] {
                  new JLabel("IP Address:"),  
                  ip_TA,  
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
            	String ipname = ip_TA.getText().trim();        	
            	
            	
	        	if(usernamename.length()!=0 && groupname.length()!=0 && ipname.length()!=0){
	        		String params = "?name="+usernamename+"&group="+groupname;
	        		ip = (ip.concat(ipname+":1018")).concat(params);
	        		
	        		System.out.println("Connecting to RHCC server at : "+ip);
	        		
	        		start(ip);
	        	} else {
	        		String params = "?name="+"m"+"&group="+"k";
	        		ip = (ip.concat("192.168.1.2:1018")).concat(params);
	        		
	        		System.out.println("Connecting to RHCC server at : "+ip);
	        		
	        		start(ip);
	        	}
	        	
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}            
        } else {  
        	window.dispose();
        }  
        window.requestFocus();
	}
	
	
	// Builds the GUI including image frames and events registration
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
			
		window.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {								
			}

			
			@Override
			public void keyReleased(KeyEvent arg0) {
					
				if(arg0.getKeyCode() == 79) {
					// press shift o
					optimizePoints();
				}
				
				if(arg0.getKeyCode() == 88) {
					// press shift x
					stop();
					window.dispose();
					System.exit(0);
				}
				
				if(arg0.getKeyCode() == 83) {
					// press shift s
					save();
				}
				
				if(arg0.getKeyCode() == 27) {
					// press Esc
					stop();
					window.dispose();
					System.exit(1);
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
	
	
	// initially called to track the display area and save it for later use
	private void optimizePoints() {
		
			video.grab();
		
			ip3.updateImage(messageScreen);			
			try {
				Thread.sleep(7000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
			
			ip3.updateImage(whiteScreen);			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			video.grab();
			video.retrieve(frameInternal);
			
			Mat threeChannel = new Mat();
	        Imgproc.cvtColor(frameInternal, threeChannel, Imgproc.COLOR_BGR2GRAY);
	        Imgproc.threshold(threeChannel, threeChannel, 100, 255, Imgproc.THRESH_BINARY);
	        
	        Mat fg = new Mat(frameInternal.size(),CvType.CV_8U);
	        Imgproc.erode(threeChannel,fg,new Mat(),new Point(-1,-1),2);

	        Mat bg = new Mat(frameInternal.size(),CvType.CV_8U);
	        Imgproc.dilate(threeChannel,bg,new Mat(),new Point(-1,-1),3);
	        
	        Imgproc.threshold(bg,bg,1, 128,Imgproc.THRESH_BINARY_INV);

	        Mat markers = new Mat(frameInternal.size(),CvType.CV_8U, new Scalar(0));
	        Core.add(fg, bg, markers);
	        
	        WatershedSegmenter segmenter = new WatershedSegmenter();
	        segmenter.setMarkers(markers);
	        Mat markedResult = segmenter.process(frameInternal);

	        Highgui.imwrite("marked.png",markedResult);
	        
			Mat imageBlurr = new Mat(markedResult.size(), Core.DEPTH_MASK_8U);
		    Mat imageAB = new Mat(markedResult.size(), Core.DEPTH_MASK_ALL);
		    
		    Imgproc.GaussianBlur(markedResult, imageBlurr, new Size(5,5), 0);
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
		    	
		    	contours.get(max_index).convertTo(conto, CvType.CV_32FC2);
		    	
		    	RotatedRect rrect = Imgproc.minAreaRect(conto);  
		     	
		     	if(rrect.size.height > rrect.size.width) {
		     		rrect.size = new Size(rrect.size.height, rrect.size.width);
		     		rrect.angle = 90 + rrect.angle;    
		     	}	     	
		    	setBackupRotatedRect(rrect);
		    }
		    else {
		    	setBackupRotatedRect(new RotatedRect(new Point(WIDTH/2, HEIGHT/2), new Size(WIDTH, HEIGHT), 0));
		    }
	}

	
	// starts the GUI Update thread
	private void startGUIUpdate()
	{
		new Thread(new Runnable(){
			
			@Override
			public void run()
			{
				Mat frameResizedInternal = new Mat(HEIGHT,WIDTH,CvType.CV_8UC3);
				byte[] byteArrayInternal;				        
		        MatOfByte matOfByteInternal = new MatOfByte();
		        BufferedImage bufImageCombined = null;
		        while(guiUpdate == true)
				{
					bufImageCombined = ImageUtils.stringToImage(receiveStringShared);
			        
			        if(bufImageCombined != null) {
			        	ip3.updateImage(bufImageCombined);
			        
			        }					
					
					try {
						Thread.sleep(40);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					video.grab();
					try{
						video.retrieve(frameInternal);			        			        			        
					}catch(Exception e){						
					}		
						
						
			        Point pt[] = new Point[4];			    
			        backupRrect.points(pt);
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
					    if(webclient.isOpen() && sendStringShared != "") {
							webclient.send(sendStringShared);																			
						}
			        }
				}
			}				
					
			
		}).start();
	}
	
	
	// starts the Web Socket Updation thread
	private void startWebSocketUpdate()
	{
		new Thread(new Runnable(){
				
			@Override
			public void run()
			{			
				while(websocketUpdate == true) 
				{					
					Mat frameExternal = null;					
			     	BufferedImage croppedRecBufferedImage = ImageUtils.stringToImage(webclient.getMessage());		
			     	
			     	if(!webclient.getMessage().equals("/") && croppedRecBufferedImage != null) {				     		
						    setReceiveStringShared(webclient.getMessage());						    					        
					        try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
			     	}			     					
				}
			}
			
		}).start();
	}
	
	
	// called when the user enters credentials and connects to the server, optimizes and starts the threads
	private void start(String loc) throws URISyntaxException
	{
		Draft[] drafts = { new Draft_17(), new Draft_10(), new Draft_76(), new Draft_75() };
		System.out.println("Connecting to Server: "+loc);			
		webclient = new WebClient(new URI(loc), drafts[0]);
		if(begin == false)
		{
			webclient.connect();
			video = new VideoCapture(0);						
			
			optimizePoints();
			startGUIUpdate();
			startWebSocketUpdate();
			begin = true;
		}
	}
	
	
	// called when user tries to save the image, saves the image in local disk
	private void save()
	{
		
		BufferedImage received = ImageUtils.stringToImage(receiveStringShared);
		BufferedImage sent = ImageUtils.stringToImage(sendStringShared);
						
		if( received != null && sent != null)
		{
			Mat matReceived = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
			Mat matSent = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);

			byte[] data = ((DataBufferByte) received.getRaster().getDataBuffer()).getData();
			matReceived.put(0, 0, data);

			byte[] data1 = ((DataBufferByte) sent.getRaster().getDataBuffer()).getData();
			matSent.put(0, 0, data1);

			Core.addWeighted(matSent, 0.6, matReceived, 0.4, 5, matReceived);
			
			Highgui.imwrite("savedStar.png",matReceived);
			System.out.println("Image Saved ...");
		}else
		{
			System.out.println("Cannot save Image...");
		}
	}
	
	
	// stops the ongoing collaboration
	private void stop()
	{		
		if(begin == true) {
			guiUpdate = false;
			websocketUpdate = false;
			webclient.close();
			video.release();			
		}		
	}
	
	
	// crops the image according to the saved location in optimize function
	public Mat imageTransform(Mat original)
	{				     	
		RotatedRect rrect = backupRrect; 	  
	     
     	Rect rect = rrect.boundingRect();

     	if(rect.x < 0) 
     		rect.x = 0;
     	if(rect.y < 0) 
     		rect.y = 0;
     	
     	Point[] transPt = transform(rect,rrect);    	
     	sortCorners(transPt, rrect.center);    	    	    	
     	
     	Mat transformMatrix = Imgproc.getRotationMatrix2D(rrect.center, rrect.angle, 1.0);
     	Mat rotated = new Mat(original.rows(),original.cols(),original.type());
     	Mat cropped = new Mat();    	
     	
     	Imgproc.warpAffine(original, rotated, transformMatrix, rotated.size());    		
     	Imgproc.getRectSubPix(rotated, rrect.size, rrect.center, cropped);    	    		        	      
 	    
     	return cropped;     	     	     	     		     		 	    	    	    	   
	}
	
	
	// sets the send string to be sent over the network to the server
	private void setSendStringShared(String string) 
	{
		synchronized(sendStringShared) 
		{
			sendStringShared = string;
		}
	}
	
	
	// sets the receive string received from the server 
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
						
		Main2 m = new Main2();
	}
	
	
	// transforms the rotated rectangle detected to an up-right rectangle
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
	
	
	// sorts the corners detected and arranges them as TL,TR,BL,BR
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
	
	
	// finds the Eucledian distance between two points one and two
	public static double distance(Point one, Point two)
	{
		double x1 = one.x;double y1 = one.y;
		double x2 = two.x;double y2 = two.x;
		
		return Math.sqrt(((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)));
	}
	
	
	// applies transform on points based on angle and center to get the up-right rectangle
	public static Point rotateAngle(Point p, Point center, double theta) 
	{
		Point rp = new Point();
		rp.x = Math.cos(theta) * (p.x - center.x) - Math.sin(theta) * (p.y - center.y) + center.x;
		rp.y = Math.sin(theta) * (p.x - center.x) + Math.cos(theta) * (p.y - center.y)+ center.y;  		
		return rp;
	}
	
	
	// sets the rectangle detected to be used later by mageTransform
	public void setBackupRotatedRect(RotatedRect rrect) {
		synchronized (backupRrect) {
			backupRrect = rrect;	
		}		
	}
	
			
}


// class for the Web Sockets
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