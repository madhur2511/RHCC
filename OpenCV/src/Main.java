import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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

public class Main
{
  static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
  
  private WebSocketClient wsc;
  
  private JFrame window;
  private JButton start, stop;
  private ImagePanel ip1;
  private ImagePanel ip2;
  private ImagePanel ip3;
  private JPanel buttonpanel;
  private Boolean begin = false;
  private VideoCapture video = null;
  private CaptureThread thread = null;
  private Mat framesrc = new Mat();
  private Mat dst = new Mat();
  
  public Main(String defaultloc)
  {
    buildGUI();

	Draft[] drafts = { new Draft_17(), new Draft_10(), new Draft_76(), new Draft_75() };		
	try {
			wsc = new WebSocketClient( new URI( defaultloc ), drafts[0] ) {
			@Override
			public void onMessage( String message ) {
				//System.out.println("Message :" + message);
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
		};
		wsc.connect();				
	
	} catch ( URISyntaxException ex ) {
		System.out.println("not a valid WebSocket URI\n" );
	}			
    
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
	        start();
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
  
  
  private void start()
  {
    System.out.println("Starting...");
    
    if(begin == false)
    {
      video = new VideoCapture(0);

      if(video.isOpened())
      {
        thread = new CaptureThread(this);
        thread.start();
        begin = true;
      }
    }
  }
  
  
  private void stop()
  {
    System.out.println("Stopping...");
    begin = false;
    try{ Thread.sleep(1000); } catch(Exception ex){}
    video.release();
    wsc.close();
  }

  
  public static void main(String[] args) throws IOException, InterruptedException
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
	
	Main obj = new Main(loc);
	Thread.sleep(1000);
	
}

  
  class CaptureThread extends Thread
  {
	private Main obj;
	
	public CaptureThread(Main obj){
		this.obj = obj;
	}
	  
    @Override
    public void run()
    {
      if(video.isOpened())
      {
        while(begin == true)
        {
          video.read(framesrc);
          video.retrieve(framesrc);
          
          MatOfByte matOfByte_src= new MatOfByte();
          MatOfByte matOfByte_dst= new MatOfByte();
          BufferedImage bufImage_src = null;
          BufferedImage bufImage_dst = null;
          Mat frame_src = new Mat(240,320,CvType.CV_8UC3);
          Mat frame_dst = new Mat(240,320,CvType.CV_8UC3); 
          InputStream in1,in2;
          
          Core.addWeighted(framesrc, 0.5, framesrc, 0.5, 50.0, dst);
          
          Imgproc.resize(framesrc, frame_src, frame_src.size());
          Imgproc.resize(dst, frame_dst, frame_dst.size());
          
          Highgui.imencode(".jpg", frame_src, matOfByte_src);
          byte[] byteArray_src = matOfByte_src.toArray();

          Highgui.imencode(".jpg", frame_dst, matOfByte_dst);
          byte[] byteArray_dst = matOfByte_dst.toArray();
          
          try
          {
            in1 = new ByteArrayInputStream(byteArray_src);
            bufImage_src = ImageIO.read(in1);
            
            String base64 = ImageUtils.imageToString(bufImage_src,"jpg");
            obj.wsc.send(base64);
            
            in2 = new ByteArrayInputStream(byteArray_dst);
            bufImage_dst = ImageIO.read(in2);
          }
          catch(Exception ex)
          {
            ex.printStackTrace();
          }
          
          ip1.updateImage(bufImage_src);
          ip2.updateImage(bufImage_src);
          ip3.updateImage(bufImage_dst);
          
          try{ Thread.sleep(5); } catch(Exception ex){}
        }
      }
    }
  }  
}
