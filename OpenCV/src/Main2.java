import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class Main2 {
	
	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	private JFrame window;
	private JButton start, stop;
	private ImagePanel ip1;
	private ImagePanel ip2;
	private ImagePanel ip3;
	private JPanel buttonpanel;
	private VideoCapture video = null;	
	private WebClient webclient;
	private Boolean begin = false;
	private Mat frameInternal = new Mat();
	private Mat frameExternal = new Mat();
	private String defaultloc;
	
	public Main2(String defaultloc) throws URISyntaxException
	{
		buildGUI();
		this.defaultloc = defaultloc;
	}		
	
	public void buildGUI()
	{
	    window = new JFrame("Realtime Content Collaboration");
	    
	    buttonpanel = new JPanel();
		buttonpanel.setLayout(null);
	    
	    ip1 = new ImagePanel();
		ip1.setBounds(150,10,200,160);
		ip1.setBorder(BorderFactory.createLineBorder(Color.black));
		window.add(ip1);
		
		ip2 = new ImagePanel();
		ip2.setBounds(400,10,200,160);
		ip2.setBorder(BorderFactory.createLineBorder(Color.black));
		window.add(ip2);
		
		ip3 = new ImagePanel();
		ip3.setBounds(275,180,200,160);
		ip3.setBorder(BorderFactory.createLineBorder(Color.black));
		window.add(ip3);
	    
	    
	    start = new JButton("Start");
		start.setBounds(10, 10, 70, 30);
		start.addActionListener(new ActionListener(){
		      @Override
		      public void actionPerformed(ActionEvent e){
		        try {
					start();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
		      }
		});
		buttonpanel.add(start);
		
		stop = new JButton("Stop");
		stop.setBounds(10, 50, 70, 30);
		stop.addActionListener(new ActionListener(){
		      @Override
		      public void actionPerformed(ActionEvent e){
		        stop();
		      }
		});
		buttonpanel.add(stop);
		window.add(buttonpanel);
		
	    Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screen = tk.getScreenSize();
		int width = screen.width;
		int height = screen.height;
		
		window.setSize(width/2,height/2);
		window.setLocation(width/2-window.getSize().width/2, height/2-window.getSize().height/2);
		
	    window.setVisible(true);
	    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    window.setResizable(false);
	}
	
	private void start() throws URISyntaxException
	{
		Draft[] drafts = { new Draft_17(), new Draft_10(), new Draft_76(), new Draft_75() };
		webclient = new WebClient(new URI( defaultloc ), drafts[0]);
		if(begin == false)
		{
			webclient.connect();
			video = new VideoCapture(0);			
			begin = true;
			new Thread(new Runnable(){
				
				@Override
				public void run()
				{
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					
					BufferedImage bufImageInternal = null;
			        BufferedImage bufImageExternal;
			        BufferedImage bufImageCombined;
			        
			        byte[] byteArrayInternal;				        
			        MatOfByte matOfByteInternal;
								        			        				        
					while(begin == true)
					{
						synchronized(begin){
							// Internal frame read
							video.read(frameInternal);
					        video.retrieve(frameInternal);
					        
					        Mat frameTempInternal = new Mat(240,320,CvType.CV_8UC3);
					        Imgproc.resize(frameInternal, frameTempInternal, frameTempInternal.size());
					        matOfByteInternal= new MatOfByte();
					        
					        synchronized(webclient)
					        {
						        bufImageExternal = ImageUtils.stringToImage(webclient.getMessage());
						        
						        if(!(webclient.getMessage().equals("/")) && (bufImageExternal != null)) 
						        {						        	
							        //External frame read
						        	
							        byte[] data = ((DataBufferByte) bufImageExternal.getRaster().getDataBuffer()).getData();
									frameExternal = new Mat(240, 320, CvType.CV_8UC3);
									frameExternal.put(0, 0, data);					        				        											        						        
							        
							        Mat frameTempExternal = new Mat(240,320,CvType.CV_8UC3);
							        Imgproc.resize(frameExternal, frameTempExternal, frameTempExternal.size());
							        
							        Mat frameTempCombined = new Mat(240,320,CvType.CV_8UC3);						        
							        Core.addWeighted(frameTempInternal, 0.5, frameTempExternal, 0.5, 10.0, frameTempCombined);
							        						        
							        MatOfByte matOfByteCombined= new MatOfByte();						        						       
								
								    Highgui.imencode(".jpg", frameTempCombined, matOfByteCombined);
								    byte[] byteArrayCombined = matOfByteCombined.toArray();
								    
								    try
								    {					    								        
								        bufImageCombined = ImageIO.read(new ByteArrayInputStream(byteArrayCombined));								        							        							        							       
								        ip2.updateImage(bufImageExternal);
								        ip3.updateImage(bufImageCombined);							        							        									        
								    }
								    catch(Exception e)
								    {
								    	
								    }
						        }
						        
						        Highgui.imencode(".jpg", frameTempInternal, matOfByteInternal);
							    byteArrayInternal = matOfByteInternal.toArray();
							    try {
									bufImageInternal = ImageIO.read(new ByteArrayInputStream(byteArrayInternal));
									String base64 = ImageUtils.imageToString(bufImageInternal,"jpg");
							        webclient.send(base64);
							        
								} catch (IOException e) {
									e.printStackTrace();
								}
						        ip1.updateImage(bufImageInternal);
					        }						    					   
						}
			        }
				}
			}).start();			
		}
	}
	
	private void stop()
	{
		synchronized(begin) {
			begin = false;
			webclient.close();
			video.release();	
		}
	}
	
	public static void main(String args[]) throws NumberFormatException, IOException, URISyntaxException
	{
		WebSocketImpl.DEBUG = false;
		String loc;
		int port=8889;
		BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Madhur/workspace/port.txt"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			port = Integer.parseInt(line);
		}
		
		if( args.length != 0 ) {
			loc = args[ 0 ];
			System.out.println( "Default server url specified: \'" + loc + "\'" );
		} else {
			loc = "ws://localhost:"+port;
			System.out.println( "Default server url not specified: defaulting to \'" + loc + "\'" );
		}
						
		Main m3 = new Main(loc);
	}
			
}

/*class WebClient extends WebSocketClient
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
		System.out.println( "You are connected to ChatServer: " + getURI() + "\n" );						
	}

	@Override
	public void onClose( int code, String reason, boolean remote ) {
		System.out.println("Closed");		
	}

	@Override
	public void onError( Exception ex ) {
		System.out.println(ex.toString());
	}	
}*/