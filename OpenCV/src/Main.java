//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.image.BufferedImage;
//import java.awt.image.DataBufferByte;
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//
//import javax.imageio.ImageIO;
//import javax.swing.BorderFactory;
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JSlider;
//import javax.swing.JTextField;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//
//import org.java_websocket.WebSocketImpl;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.drafts.Draft;
//import org.java_websocket.drafts.Draft_10;
//import org.java_websocket.drafts.Draft_17;
//import org.java_websocket.drafts.Draft_75;
//import org.java_websocket.drafts.Draft_76;
//import org.java_websocket.handshake.ServerHandshake;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfByte;
//import org.opencv.core.Size;
//import org.opencv.highgui.Highgui;
//import org.opencv.highgui.VideoCapture;
//import org.opencv.imgproc.Imgproc;
//
//public class Main {
//	
//	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
//	public static String ip= "ws://192.168.1.2";
//	private JFrame window;
//	private JButton start, stop, save;
//	private ImagePanel ip1;
//	private ImagePanel ip3;
//	private JPanel buttonpanel;
//	private static JTextField username,group;
//	private VideoCapture video = null;	
//	private WebClient webclient;
//	private Boolean begin = false;
//	private Mat frameInternal = new Mat();
//	private String defaultloc;
//	private final static int WIDTH = 640;
//	private final static int HEIGHT = 480;
//	
//	public Main(String defaultloc) throws URISyntaxException
//	{
//		buildGUI();
//		this.defaultloc = defaultloc;
//	}		
//	
//	public void buildGUI()
//	{
//	    window = new JFrame("Realtime Content Collaboration");
//	    
//	    Toolkit tk = Toolkit.getDefaultToolkit();
//		Dimension screen = tk.getScreenSize();
//		int width = screen.width;
//		int height = screen.height;
//		
//		System.out.println(width + " x " + height);
//	    
//	    buttonpanel = new JPanel();
//		buttonpanel.setLayout(null);
//		
//		username = new JTextField(10);
//		TextPrompt usernameprompt = new TextPrompt("Username", username); 
//		username.setText("");
//		username.setBounds(10, height-80, 100, 20);
//		window.add(username);
//		
//		group = new JTextField(10);
//		TextPrompt groupprompt = new TextPrompt("Group", group);
//		group.setText("");
//		group.setBounds(120, height-80, 100, 20);
//		window.add(group);
//		
//	    
//	    ip1 = new ImagePanel();
//		ip1.setBounds(10,10,200,160);
//		ip1.setBorder(BorderFactory.createLineBorder(Color.black));
//		window.add(ip1);
//		
//		ip3 = new ImagePanel();
//		ip3.setBounds(275,10,width-300,height-120);
//		ip3.setBorder(BorderFactory.createLineBorder(Color.black));
//		window.add(ip3);
//	    
//	    start = new JButton("Start");
//	    start.setBounds(230, height-85, 70, 30);
//		start.addActionListener(new ActionListener(){
//		      @Override
//		      public void actionPerformed(ActionEvent e){
//		        try {
//		        	String ip = Main.ip;
//		        	String usernamename = Main.username.getText();
//		        	
//		        	String groupname = Main.group.getText();
//		        	
//		        	if(usernamename.length()!=0 && groupname.length()!=0){
//		        		String params = "?name="+usernamename+"&group="+groupname;
//		        		start(ip.concat(params));
//		        	}
//		        	
//				} catch (URISyntaxException e1) {
//					e1.printStackTrace();
//				}
//		      }
//		});
//		buttonpanel.add(start);
//		
//		stop = new JButton("Stop");
//		stop.setBounds(310, height-85, 70, 30);
//		stop.addActionListener(new ActionListener(){
//		      @Override
//		      public void actionPerformed(ActionEvent e){
//		        stop();
//		      }
//		});
//		buttonpanel.add(stop);
//		
//		save = new JButton("Save");
//		save.setBounds(390, height-85, 70, 30);
//		save.addActionListener(new ActionListener(){
//		      @Override
//		      public void actionPerformed(ActionEvent e){
//		        try {
//					save();
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//		      }
//		});
//		buttonpanel.add(save);
//		
//        window.add(buttonpanel);
//		
//	    window.setSize(width,height);
//		window.setVisible(true);
//	    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	    window.setResizable(false);
//	}
//	
//	private void start(String loc) throws URISyntaxException
//	{
//		Draft[] drafts = { new Draft_17(), new Draft_10(), new Draft_76(), new Draft_75() };
//		System.out.println("Connecting to Server: "+loc);
//		webclient = new WebClient(new URI(loc), drafts[0]);
//		if(begin == false)
//		{
//			webclient.connect();
//			video = new VideoCapture(0);			
//			begin = true;
//			new Thread(new Runnable(){
//				
//				@Override
//				public void run()
//				{
//					try {
//						Thread.sleep(2000);
//					} catch (InterruptedException e1) {
//						e1.printStackTrace();
//					}
//					
//					BufferedImage bufImageInternal = null;
//			        BufferedImage bufImageCombined;
//			        
//			        byte[] byteArrayInternal;				        
//			        MatOfByte matOfByteInternal;
//								        			        				        
//					while(begin == true)
//					{
//						synchronized(begin){
//							// Internal frame read
//							
//							video.read(frameInternal);
//					        video.retrieve(frameInternal);
//					        
//					        Mat frameTempInternal = new Mat(HEIGHT,WIDTH,CvType.CV_8UC3);
//					        Imgproc.resize(frameInternal, frameTempInternal, frameTempInternal.size());
//					        
//					        matOfByteInternal= new MatOfByte();
//					        
//					        synchronized(webclient)
//					        {
//						        bufImageCombined = ImageUtils.stringToImage(webclient.getMessage());
//						        
//						        if(!(webclient.getMessage().equals("/")) && (bufImageCombined != null)) 
//						        {						        	
//								    try
//								    {					    								         
//								        ip3.updateImage(bufImageCombined);							        							        									        
//								    }
//								    catch(Exception e)
//								    {
//								    	
//								    }
//						        }
//						        
//						        if(webclient.getMessage().contains("new connection")){
//						        	System.out.println(webclient.getMessage().toString());
//						        }
//						        
//						        if(webclient.getMessage().contains("left the collaboration")){
//						        	System.out.println(webclient.getMessage().toString());
//						        }
//						        
//					        	
//						        Highgui.imencode(".png", frameTempInternal, matOfByteInternal);
//							    byteArrayInternal = matOfByteInternal.toArray();
//							    try {
//									bufImageInternal = ImageIO.read(new ByteArrayInputStream(byteArrayInternal));
//									String base64 = ImageUtils.imageToString(bufImageInternal,"png");
//									
//									if(webclient.isOpen())
//										webclient.send(base64);
//								} catch (IOException e) {
//									e.printStackTrace();
//								}
//							    ip1.updateImage(bufImageInternal);
//					        }						    					   
//						}
//			        }
//				}
//			}).start();			
//		}
//	}
//	
//	
//	private void save()
//	{
//		BufferedImage savimg = (BufferedImage)ip3.getImage();
//		if(savimg != null)
//		{
//			byte[] data = ((DataBufferByte) savimg.getRaster().getDataBuffer()).getData();
//			Mat saveframe = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
//			saveframe.put(0, 0, data);					        				        											        						        
//			Highgui.imwrite("saved1.png",saveframe);
//			System.out.println("Image Saved ...");
//		}else
//		{
//			System.out.println("Cannot save Image.. Non existant remote Connection!");
//		}
//	}
//	
//	
//	private void stop()
//	{
//		synchronized(begin) {
//			begin = false;
//			webclient.close();
//			video.release();	
//		}
//	}
//	
//	public static void main(String args[]) throws NumberFormatException, IOException, URISyntaxException
//	{
//		WebSocketImpl.DEBUG = false;
//		String loc;
//		int port=1018;
//		BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Anand/workspace/port.txt"));
//		String line = null;
//		while ((line = reader.readLine()) != null) {
//			port = Integer.parseInt(line);
//		}
//		
//		if( args.length != 0 ) {
//			loc = args[ 0 ];
//			System.out.println( "Default server url specified: \'" + loc + "\'" );
//		} else {
//			
//			Main.ip = (Main.ip).concat(":"+port);
//			
//			System.out.println( "Default server url not specified: defaulting to \'" + Main.ip + "\'" );
//		}
//						
//		
//		Main m3 = new Main(Main.ip);
//	}
//			
//}
//
//class WebClient extends WebSocketClient
//{
//	private String message = "";
//	
//	public String getMessage()
//	{
//		return message;
//	}
//	
//	public WebClient(URI uri, Draft draft)
//	{
//		super(uri, draft);
//	}
//	
//	@Override
//	public void onMessage( String message ) {
//		this.message = message;		
//	}
//
//	@Override
//	public void onOpen( ServerHandshake handshake ) {
//		System.out.println( "You are connected to RHCCServer: " + getURI() + "\n" );						
//	}
//
//	@Override
//	public void onClose( int code, String reason, boolean remote ) {
//		System.out.println("Closed because "+reason+code);
//		this.close();
//	}
//
//	@Override
//	public void onError( Exception ex ) {
//		System.out.println(ex.toString());
//	}	
//}
