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
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.RHCCServer.Model.Client;
import com.RHCCServer.Model.Group;
import com.RHCCServer.Utils.ImageUtils;
import com.RHCCServer.Utils.Utils;

public class Router extends WebSocketServer {
	
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

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the collaboration!" );
		
		String[] params = Utils.getGETParamsFromURL(handshake.getResourceDescriptor());
		
		if(params.length > 1){
			System.out.println(params[0]+" joined");
			
			Client c = new Client(params[0],conn);
			Group g = new Group(params[1]);
			Group gStar=null;
			
			for(Group giterate : groups){
				
				if(giterate.getGroupName().equalsIgnoreCase(g.getGroupName())){

					System.out.println("GROUP ALREADY EXISTS");			
					giterate.addMember(c);
					gStar = giterate;
				}
			}
			
			if(gStar == null){				
				System.out.println("NEW GROUP CREATED");

				g.addMember(c);
				groups.add(g);
				gStar = g;				
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
	public void onMessage( WebSocket conn, String message ) {
		
		ArrayList<WebSocket> conlist = null;
		Client current = getClientForConnection(conn);
		
		if(current == null){
			//Cannot merge on the server if no url provided i.e. if no group info
			this.sendToAllExcept(message, conn);
		}
		else
		{
			current.setLatestFrame(message);
			
			Group g = getGroupForClient(conn);
			
			if(g==null)
				conlist = new ArrayList<WebSocket>(this.connections());
			else
			{
				conlist = new ArrayList<WebSocket>();
				for(Client c : g.getAllMembers())
					conlist.add(c.getConn());
			}
			
			//System.out.println(conlist.size());
			
			BufferedImage bufImageCombined = null, bufImageExternal;
			Mat frameExternal;
			int i=0;
			WebSocket firstNonSource;
			Mat frameCombined = new Mat(240,320,CvType.CV_8UC3);
		
			firstNonSource = getFirstNonSourceWebSocket(conn);
			
			if(firstNonSource !=null)
			{
				Client ctemp = getClientForConnection(firstNonSource);	
				bufImageExternal = ImageUtils.stringToImage(ctemp.getLatestFrame());
				ctemp = null;
				if(bufImageExternal != null){
					byte[] data = ((DataBufferByte) bufImageExternal.getRaster().getDataBuffer()).getData();
					frameExternal = new Mat(240, 320, CvType.CV_8UC3);
					frameExternal.put(0, 0, data);
					
					bufImageExternal = null;
					
					Mat frameTempExternal = new Mat(240,320,CvType.CV_8UC3);
			        Imgproc.resize(frameExternal, frameTempExternal, frameTempExternal.size());
			        						        
			        Core.addWeighted(frameTempExternal, 0.4, frameTempExternal, 0.6, 10.0, frameCombined);
			        
		        	while(i < conlist.size()) {
		        		ctemp = getClientForConnection(conlist.get(i));	
						bufImageExternal = ImageUtils.stringToImage(ctemp.getLatestFrame());
						
				        if(!(message.equals("/")) && (bufImageExternal != null)) 
				        {						        	
					        data = ((DataBufferByte) bufImageExternal.getRaster().getDataBuffer()).getData();
							frameExternal = new Mat(240, 320, CvType.CV_8UC3);
							frameExternal.put(0, 0, data);					        				        											        						        
					        
							bufImageExternal = null;
							
					        frameTempExternal = new Mat(240,320,CvType.CV_8UC3);
					        Imgproc.resize(frameExternal, frameTempExternal, frameTempExternal.size());
					        						        
					        Core.addWeighted(frameCombined, 0.5, frameTempExternal, 0.5, 0.0, frameCombined);
				        }
						i+=1;
		        	}
		        	
			        MatOfByte matOfByteCombined= new MatOfByte();						        						       
				
				    Highgui.imencode(".jpg", frameCombined, matOfByteCombined);
				    byte[] byteArrayCombined = matOfByteCombined.toArray();
				
				    try {
						bufImageCombined = ImageIO.read(new ByteArrayInputStream(byteArrayCombined));
					} catch (IOException e) {
						e.printStackTrace();
					}								        							        							        							       
				    String base64 = ImageUtils.imageToString(bufImageCombined,"jpg");
				    conn.send(base64);
				}
		    }
		}
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