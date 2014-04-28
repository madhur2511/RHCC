package com.RHCCServer.Router;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;


import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

import com.RHCCServer.Model.Client;
import com.RHCCServer.Model.Group;
import com.RHCCServer.Utils.ImageUtils;
import com.RHCCServer.Utils.Utils;
import com.sun.org.apache.xml.internal.security.utils.Base64;


// this class represents the main routing functionality of the server
public class Router extends WebSocketServer {
	
	
	// fixed resolution of the frames
	private final static int WIDTH = 640;
	private final static int HEIGHT = 480;
	
	static { System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

	
	// a list of active groups kept by the server
	private ArrayList<Group> groups=null;

	
	public Router( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
		groups = new ArrayList<Group>();
	}

	
	public Router( InetSocketAddress address ) {
		super( address );
		groups = new ArrayList<Group>();
	}
	
	
	// Merging Unit - Merge Frames from different client
	public void sendToClientsForGroup(Group g) {	
		
		Client[] clientList = g.getAllMembers().toArray(new Client[0]);		
		while(clientList.length > 0) {					
			
			for(Client c1:clientList) {								
				
				Mat frameCombined = null;				
				int clientsWithValidImage = 0;
				
				for(Client c:clientList) {
					
					if(!c1.getConn().equals(c.getConn())) { 
						
						String frame = c.getLatestFrame();											
						BufferedImage bufImageExternal;
						Mat frameExternal;						
						bufImageExternal = ImageUtils.stringToImage(frame);
						
						if(!frame.equals("/") && bufImageExternal != null) {	
							clientsWithValidImage += 1; 
							byte[] data = ((DataBufferByte) bufImageExternal.getRaster().getDataBuffer()).getData();
						
							frameExternal = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
							frameExternal.put(0, 0, data);
					        
							if(clientsWithValidImage == 1)
					        	frameCombined = frameExternal;
					        else {
					        	Core.addWeighted(frameCombined, 0.5, frameExternal, 0.5, 0.0, frameCombined);
					        }					      					        											    										        							        							        							       				 
						}
					}
				}
				
				if(frameCombined != null) {
					MatOfByte matOfByteCombined= new MatOfByte();						        						       			        
				    Highgui.imencode(".png", frameCombined, matOfByteCombined);
				    
				    byte[] byteArrayCombined = matOfByteCombined.toArray();				    
					c1.setAddedFrame(Base64.encode(byteArrayCombined));					
				}
			}
			
			if(clientList.length > 1) {
				for(Client c:clientList) {							
					WebSocket conn = c.getConn();
					if(conn.isOpen()) {												
						conn.send(c.getAddedFrame());
						System.out.println("Sending frame to client " + c.getUsername());						
					}					
				}
			}								
			
			
			if(clientList.length < 2) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}			
			
			clientList = g.getAllMembers().toArray(new Client[0]);			
		}
	}

			
	
	// Routing Feed Unit - Has functions to receive and send frames 
	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {				
		
		String[] params = Utils.getGETParamsFromURL(handshake.getResourceDescriptor());
		
		if(params.length > 1){
			
			System.out.println(params[0]+" joined the collaboration");
			
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
									
			this.sendToAllInGroupExcept( "New connection: " + params[0], getGroupForClient(conn), conn);						
		}
		
		else {
			System.out.println(handshake.getResourceDescriptor());
			this.sendToAllExcept("New connection: " + handshake.getResourceDescriptor(),conn);
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
			this.sendToAllExcept(conn + " has left the collaboration!", conn);
		}
	}
	
	@Override
	public void onMessage( final WebSocket conn, final String message ) {
				
		Client current = getClientForConnection(conn);
		if(current != null)
			current.setLatestFrame(message);												
	}
	

	@Override
	public void onFragment( WebSocket conn, Framedata fragment ) {
		
	}
		

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = false;
		
		int port=1018;
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