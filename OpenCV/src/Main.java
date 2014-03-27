import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

public class Main {
	
	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	private JFrame window;
	private JButton start, stop, save;
	private ImagePanel ip1;
	private ImagePanel ip2;
	private ImagePanel ip3;
	private JPanel buttonpanel;
	private JSlider jslider;
	private VideoCapture video = null;	
	private WebClient webclient;
	private Boolean begin = false;
	private Mat frameInternal = new Mat();
	private String defaultloc;
	private double alpha=0.5;
	
	public Main(String defaultloc) throws URISyntaxException
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
		
		save = new JButton("Save");
		save.setBounds(10, 90, 70, 30);
		save.addActionListener(new ActionListener(){
		      @Override
		      public void actionPerformed(ActionEvent e){
		        try {
					save();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		      }
		});
		buttonpanel.add(save);
		
		
		jslider=new JSlider(JSlider.VERTICAL,0,10,5);//direction , min , max , current
		jslider.setBounds(10, 150, 70, 200);
		jslider.setFont(new Font("Tahoma",Font.BOLD,12));
        jslider.setMajorTickSpacing(1);
        jslider.setPaintLabels(true);
        jslider.setPaintTicks(true);
        jslider.setPaintTrack(true);
        jslider.setAutoscrolls(true);
        jslider.setPreferredSize(new Dimension(50,50));
        jslider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
            	alpha=(jslider.getValue())/10.0;
            }
        });
        buttonpanel.add(jslider);
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
						        bufImageCombined = ImageUtils.stringToImage(webclient.getMessage());
						        
						        if(!(webclient.getMessage().equals("/")) && (bufImageCombined != null)) 
						        {						        	
								    try
								    {					    								         
								        ip2.updateImage(bufImageCombined);
								        ip3.updateImage(bufImageCombined);							        							        									        
								    }
								    catch(Exception e)
								    {
								    	
								    }
						        }
						        
						        if(webclient.getMessage().contains("new connection")){
						        	System.out.println(webclient.getMessage().toString());
						        }
						        
						        if(webclient.getMessage().contains("left the collaboration")){
						        	System.out.println(webclient.getMessage().toString());
						        }
						      
						        Highgui.imencode(".jpg", frameTempInternal, matOfByteInternal);
							    byteArrayInternal = matOfByteInternal.toArray();
							    try {
									bufImageInternal = ImageIO.read(new ByteArrayInputStream(byteArrayInternal));
									String base64 = ImageUtils.imageToString(bufImageInternal,"jpg");
									
									if(webclient.isOpen())
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
	
	
	private void save()
	{
		BufferedImage savimg = (BufferedImage)ip3.getImage();
		if(savimg != null)
		{
			byte[] data = ((DataBufferByte) savimg.getRaster().getDataBuffer()).getData();
			Mat saveframe = new Mat(240, 320, CvType.CV_8UC3);
			saveframe.put(0, 0, data);					        				        											        						        
			Highgui.imwrite("saved.jpg",saveframe);
			System.out.println("Image Saved ...");
		}else
		{
			System.out.println("Cannot save Image.. Non existant remote Connection!");
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
			loc = "ws://192.168.2.13:"+port+"?name=Madhur&group=pescs";
			System.out.println( "Default server url not specified: defaulting to \'" + loc + "\'" );
		}
						
		Main m3 = new Main(loc);
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