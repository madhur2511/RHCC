package com.RHCCServer.Router;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.RHCCServer.Model.Client;
import com.RHCCServer.Model.Group;
import com.RHCCServer.Utils.ImageUtils;
import com.RHCCServer.Utils.Utils;

public class Router extends WebSocketServer {
	
	private final static int WIDTH = 640;
	private final static int HEIGHT = 480;
	
	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	private ArrayList<Group> groups=null;

	public Router( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
		groups = new ArrayList<Group>();
	}

	public Router( InetSocketAddress address ) {
		super( address );
		groups = new ArrayList<Group>();
	}
	
	public void sendToClientsForGroup(Group g) {
		
		Client[] clientList = g.getAllMembers().toArray(new Client[0]);		
		while(clientList.length > 0) {
			BufferedImage bufImageCombined = null;
			// merging
			int clientsWithValidImage = 0;
			String base64 = "";
			Mat frameCombined = null;
			for(Client c:clientList) {
				String frame = c.getLatestFrame();											
				BufferedImage bufImageExternal;
				Mat frameExternal;							
				bufImageExternal = ImageUtils.stringToImage(frame);
				if(!frame.equals("/") && bufImageExternal != null) {
					clientsWithValidImage += 1;
					byte[] data = ((DataBufferByte) bufImageExternal.getRaster().getDataBuffer()).getData();
					frameExternal = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
					frameExternal.put(0, 0, data);
					
					//Mat frameTempExternal = new Mat(HEIGHT,WIDTH,CvType.CV_8UC3);
			        //Imgproc.resize(frameExternal, frameTempExternal, frameTempExternal.size());
			        
					if(clientsWithValidImage == 1)
			        	frameCombined = frameExternal;
			        else {
			        	Core.addWeighted(frameCombined, 0.5, frameExternal, 0.5, 0.0, frameCombined);
			        }
			      
			        MatOfByte matOfByteCombined= new MatOfByte();						        						       			        
//			        Mat destination = new Mat();
//			        try{
//		        		
//		    	        destination = new Mat(frameCombined.rows(),frameCombined.cols(),frameCombined.type());
//		    	        Imgproc.GaussianBlur(frameCombined, destination, new Size(0,0), 20);
//		    	        Core.addWeighted(frameCombined, 1.5, destination, -0.5, 0, destination);
//			        }catch (Exception e) {
//		                  System.out.println("error: " + e.getMessage());
//		            }
		            
				    Highgui.imencode(".png", frameCombined, matOfByteCombined);
				    byte[] byteArrayCombined = matOfByteCombined.toArray();
				
				    try {
						bufImageCombined = ImageIO.read(new ByteArrayInputStream(byteArrayCombined));
						base64 = ImageUtils.imageToString(bufImageCombined,"png");
					} catch (IOException e) {
						e.printStackTrace();
					}											        							        							        							       				    
				}													
			}
			
			if(clientsWithValidImage > 0) {
				// sending
				for(Client c:clientList) {		
					WebSocket conn = c.getConn();
					if(conn != null) {
						synchronized (conn) {
							conn.send(base64);
						}
					}
					System.out.println("Sending to client" + c.getUsername());
				}
			} else {
				try {
					Thread.sleep(100);
					System.out.println("sleeping");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
			clientList = g.getAllMembers().toArray(new Client[0]);
		}
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the collaboration!" );
		
		String[] params = Utils.getGETParamsFromURL(handshake.getResourceDescriptor());
		
		if(params.length > 1){
			System.out.println(params[0]+" joined");
			
			Client c = new Client(params[0],conn);
			final Group g = new Group(params[1]);
			Group gStar=null;
			
			for(Group giterate : groups){
				
				if(giterate.getGroupName().equalsIgnoreCase(g.getGroupName())){

					System.out.println("GROUP ALREADY EXISTS");
					synchronized(giterate) {
						giterate.addMember(c);
					}
					gStar = giterate;
				}
			}
			
			if(gStar == null){				
				System.out.println("NEW GROUP CREATED");
				g.addMember(c);
				groups.add(g);
				gStar = g;				
				
				new Thread(new Runnable(){			
					@Override
					public void run()
					{
						sendToClientsForGroup(g);
					}
				}).start();
			}
			
			Group g1 = getGroupForClient(conn);
			
			this.sendToAllInGroupExcept( "new connection: " + params[0], getGroupForClient(conn), conn);						
		}
		
		else{
			System.out.println(handshake.getResourceDescriptor());
			this.sendToAllExcept("new connection: " + handshake.getResourceDescriptor(),conn);
		}
		
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
	
		Group g = getGroupForClient(conn);
		if(g != null){
			
			String leftUser = getClientForConnection(conn).getUsername();
			
			this.sendToAllInGroupExcept( leftUser + " has left the collaboration!",g,conn);
			System.out.println( leftUser + " has left the collaboration!" );
			
			for(int i=0; i<groups.size(); i++){
				Group tempGrp = groups.get(i);
				
				for(int j=0; j< tempGrp.getAllMembers().size(); ++j){
					
					Client tempCli = tempGrp.getAllMembers().get(j);
					
					if(tempCli.getConn().equals(conn)){
						tempGrp.removeMember(tempCli);
					}
				}
				if(tempGrp.getAllMembers().size() == 0){
					groups.remove(tempGrp);
				}
			}
		}
		else{
			this.sendToAllExcept(conn + " has left the collaboration!",conn);
		}
	}
	
	@Override
	public void onMessage( final WebSocket conn, final String message ) {
		
		ArrayList<WebSocket> conlist = null;
		Client current = getClientForConnection(conn);
		if(current != null)
			current.setLatestFrame(message);												
	}
	

	@Override
	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = false;
		
		int port=9000;
		port = Utils.getPortFromFile();
		
		Router s = new Router( port );
		s.start();
		System.out.println( "RHCCServer started on port: " + s.getPort() );
	}
	
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			
		}
	}

	
	public WebSocket getFirstNonSourceWebSocket(WebSocket conn)
	{
		WebSocket firstNonSource = null;
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for(WebSocket c : con){
				if(c.equals(conn))
					continue;
				firstNonSource = c;
				break;
			}
		}
		return firstNonSource;
	}
	
	
	public void sendToAll( String text ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}
	
	
	public void sendToAllExcept( String text,WebSocket conn ) {
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				if(c.equals(conn)){
					continue;
				}
				c.send( text );
			}
		}
	}
	
	
	
	public void sendToAllInGroupExcept(String text , Group g, WebSocket conn){
			for(Client ctemp : g.getAllMembers()){
				if(ctemp.getConn().equals(conn)){
					continue;
				}
				ctemp.getConn().send(text);				
			}
	}
	
	
	public Group getGroupForClient(WebSocket conn){
		for(Group gtemp:groups){
			for(Client ctemp : gtemp.getAllMembers()){
				if(ctemp.getConn().equals(conn)){
					return gtemp;
				}
			}
		}
		return null;
	}
	
	public Client getClientForConnection(WebSocket conn){
		for(Group gtemp:groups){
			for(Client ctemp : gtemp.getAllMembers()){
				if(ctemp.getConn().equals(conn)){
					return ctemp;
				}
			}
		}
		return null;
	}
	
}